package com.sh.bdt.service;

import com.sh.bdt.dto.req.LikeRequest;
import com.sh.bdt.dto.req.LikeRequestV2;
import com.sh.bdt.expection.LikeConflictException;
import com.sh.bdt.repository.PostLikeRepository;
import com.sh.bdt.repository.PostRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;

    @Transactional
    public void like(LikeRequest request) {

        // case: fk가 없는 상태에서 insert 시도
        int result = postLikeRepository.insertIgnore(request.postId(), request.userId());

        if (result > 0) { // case: 동일 postLike가 미존재
            int updatedCount = postRepository.increaseAtomicLikeCount(request.postId());

            if (updatedCount == 0) { // case: 부모 Post가 없으므로 rollback
                throw new EntityNotFoundException("Post not found.");
            }

            // case: 부모 Post가 있으므로 commit
        }

        // case: 동일 postLike가 존재 - DB에서 ignored
    }

    // ========== v2 ==========

    private final StringRedisTemplate stringRedisTemplate;
    private final RedisScript<Long> likeScript;

    public void likeV2(LikeRequestV2 request) {

        // @Transactional 제거: Redis가 Atomic한 상태
        Long postId = request.postId();
        Long userId = request.userId();
        int newStatus = request.status(); // 1: 좋아요, 0: 취소

        // KEYS[1]: 포스트별 유저들의 상태 (Map<PostId, Map<UserId, Status>>)
        String statusKey = "post:like:status:" + postId;
        // KEYS[2]: 모든 포스트의 좋아요 총합 (Map<PostId, Count>)
        String countKey = "post:like:count";
        // KEYS[3]: 변경된 PostId 내역 (Set<PostId>)
        String changedKey = "post:like:changed";

        Long result = stringRedisTemplate.execute(
            likeScript,
            List.of(statusKey, countKey, changedKey),
            String.valueOf(userId),
            String.valueOf(newStatus),
            String.valueOf(postId)
        );

        if (result == -1) {
            throw new LikeConflictException();
        }
    }
}
