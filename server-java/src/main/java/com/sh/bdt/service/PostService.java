package com.sh.bdt.service;

import com.sh.bdt.dto.req.LikeRequest;
import com.sh.bdt.entity.Post;
import com.sh.bdt.repository.PostLikeRepository;
import com.sh.bdt.repository.PostRepository;
import com.sh.bdt.thymeleaf.dto.res.PostResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
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

    public PostResponse getPost(Long postId) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new EntityNotFoundException("Post not found."));

        return new PostResponse(post.getId(), post.getLikeCount());
    }
}
