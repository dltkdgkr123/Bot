package com.sh.bdt.batch;

import com.sh.bdt.property.RedisKeyProperties;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostLikeBatchService {

    private final PostLikeBatchProcessor postLikeBatchProcessor;
    private final StringRedisTemplate redisTemplate;
    private final RedisScript<List> likeBatchProgressScript;
    private final RedisKeyProperties redisKeyProperties;

    private final String BATCH_SIZE = "5000";

    @Scheduled(fixedDelay = 1000)
    public void run() {
        while (true) {
            // 1. 루아 스크립트로 batch_seq 확보 및 원자적 스냅샷 생성
            List<Object> result = redisTemplate.execute(
                likeBatchProgressScript,
                List.of(
                    redisKeyProperties.postLike().batch().metaTime(),
                    redisKeyProperties.postLike().batch().metaSeq(),
                    redisKeyProperties.postLike().batch().queueSet()),
                String.valueOf(System.currentTimeMillis()), BATCH_SIZE
            );

            if (result == null || result.isEmpty() || "0".equals(result.get(1).toString())) {
                break;
            }

            String hexBatchSeq = result.get(0).toString();
            long batchSeq = Long.parseUnsignedLong(hexBatchSeq, 16);
            log.info("in progress batchSeq:{} (Hex: {})", batchSeq, hexBatchSeq);
            String snapshotKey = "post-like:batch:snapshot:" + hexBatchSeq;

            try {
                // 2. 스냅샷 데이터 인출 및 플랫 DTO로 변환
                Set<String> rawData = redisTemplate.opsForSet().members(snapshotKey);
                if (rawData == null || rawData.isEmpty()) {
                    break;
                }

                List<PostLikeBatchChunk> chunks = rawData.stream()
                    .map(s -> s.split(":"))
                    .map(arr -> new PostLikeBatchChunk(
                        batchSeq,
                        Long.parseLong(arr[0]), // postId
                        Long.parseLong(arr[1]), // userId
                        Integer.parseInt(arr[2]) // status
                    )).toList();

                // 3. 배치 실행
                postLikeBatchProcessor.execute(chunks);

            } catch (Exception e) {
                log.error("Batch Unit Failed for seq: {}", batchSeq, e);
                break;
            }
        }
    }
}
