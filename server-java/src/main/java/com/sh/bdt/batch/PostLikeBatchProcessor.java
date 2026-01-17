package com.sh.bdt.batch;

import com.sh.bdt.property.RedisKeyProperties;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostLikeBatchProcessor {

    private final JdbcTemplate jdbcTemplate;
    private final StringRedisTemplate redisTemplate;
    private final RedisScript<Boolean> likeBatchAckScript;
    private final RedisKeyProperties redisKeyProperties;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void execute(List<PostLikeBatchChunk> chunks) {
        if (chunks.isEmpty()) {
            return;
        }

        long batchSeq = chunks.getFirst().batchSeq();
        String hexBatchSeq = Long.toHexString(batchSeq);
        String stagingTable = "stage_like_batch_" + batchSeq;

        try {
            // 1. 고유 스테이징 테이블 생성 (인스턴스 간 격리 및 병렬 처리 보장)
            prepareStagingTable(stagingTable);

            // 2. 플랫 DTO 벌크 인서트 (JdbcTemplate batchUpdate 기반)
            saveToStaging(stagingTable, chunks);

            // 3. 비즈니스 로직 수행 (포스트 카운트 증분 및 좋아요 상태 Upsert)
            updatePost(stagingTable);
            upsertPostLike(stagingTable);

            // 4. DB 커밋 성공 시에만 Redis 스냅샷 파기 및 상태 전이 수행 (Send ACK)
            TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        sendAck(hexBatchSeq);
                    }
                });
        } catch (Exception e) {
            log.error("Batch Processor Error at seq: {}", batchSeq, e);
            throw e; // 트랜잭션 롤백 유도
        } finally {
            // 스테이징 테이블 삭제 (성공/실패 여부와 상관없이 리소스 회수)
            jdbcTemplate.execute("DROP TABLE IF EXISTS " + stagingTable);
        }
    }

    private void prepareStagingTable(String tableName) {
        jdbcTemplate.execute(String.format("""
            CREATE TABLE %s (
                post_id BIGINT,
                user_id BIGINT,
                status TINYINT,
                batched_at BIGINT,
                PRIMARY KEY (post_id, user_id)
            ) ENGINE=InnoDB
            """, tableName));
    }

    private void saveToStaging(String tableName, List<PostLikeBatchChunk> chunks) {
        String sql = String.format(
            "INSERT INTO %s (post_id, user_id, status, batched_at) VALUES (?, ?, ?, ?)",
            tableName);

        List<Object[]> params = chunks.stream()
            .map(c -> new Object[]{c.postId(), c.userId(), c.status(), c.batchSeq()})
            .toList();

        jdbcTemplate.batchUpdate(sql, params);
    }

    private void updatePost(String stagingTableName) {
        // 스테이징 테이블 내에서 post_id 그룹 별 delta를 합산하여 원본 테이블 UPDATE 횟수 최소화
        String sql = String.format("""
            UPDATE post p
            JOIN (
                SELECT t.post_id, MAX(t.batched_at) AS max_batched_at,
                       SUM(CASE
                           WHEN COALESCE(pl.batched_at, 0) >= t.batched_at THEN 0
                           WHEN COALESCE(pl.status, 0) = 0 AND t.status = 1 THEN 1
                           WHEN COALESCE(pl.status, 0) = 1 AND t.status = 0 THEN -1
                           ELSE 0
                       END) AS delta
                FROM %s t
                LEFT JOIN post_like pl ON t.post_id = pl.post_id AND t.user_id = pl.user_id
                GROUP BY t.post_id
            ) d ON p.id = d.post_id
            SET p.like_count = p.like_count + d.delta,
                p.batched_at = GREATEST(COALESCE(p.batched_at, 0), d.max_batched_at)
            WHERE d.delta <> 0 OR d.max_batched_at > COALESCE(p.batched_at, 0)
            """, stagingTableName);
        jdbcTemplate.update(sql);
    }

    private void upsertPostLike(String stagingTableName) {
        // batched_at(batchSeq) 비교를 통한 멱등성 보장형 Upsert
        String sql = String.format("""
            INSERT INTO post_like (post_id, user_id, status, batched_at)
            SELECT post_id, user_id, status, batched_at FROM %s
            ON DUPLICATE KEY UPDATE
                status = IF(VALUES(batched_at) > post_like.batched_at, VALUES(status), post_like.status),
                batched_at = IF(VALUES(batched_at) > post_like.batched_at, VALUES(batched_at), post_like.batched_at)
            """, stagingTableName);
        jdbcTemplate.update(sql);
    }

    private void sendAck(String hexBatchSeq) {
        String snapshotKey = redisKeyProperties.postLike().batch().snapshotPrefix() + hexBatchSeq;
        String statusKey = redisKeyProperties.postLike().batch().statusPrefix() + hexBatchSeq;

        redisTemplate.execute(
            likeBatchAckScript,
            List.of(snapshotKey, statusKey)
        );
        log.info("Batch ACK Success: seq={}", hexBatchSeq);
    }
}
