package com.sh.bdt.ch03;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sh.bdt.dto.req.PostLikeRequest;
import com.sh.bdt.entity.Post;
import com.sh.bdt.repository.PostLikeRepository;
import com.sh.bdt.repository.PostRepository;
import com.sh.bdt.service.PostService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Tag("ch03")
@SpringBootTest
class LikeTests {

    @Autowired
    PostService postService;
    @Autowired
    PostRepository postRepository;
    @Autowired
    PostLikeRepository postLikeRepository;

    @Test
    @Transactional
    void like_shouldIncreaseLikeCountOnlyOnce_whenCalledMultipleTimes() { // 멱등성 검증
        // given
        Post post = Post.create();
        postRepository.save(post);

        Long postId = post.getId();
        Long userId = 100L;
        PostLikeRequest request = new PostLikeRequest(postId, userId);

        // when
        postService.like(request);
        postService.like(request);
        postService.like(request);

        // then
        Post updatedPost = postRepository.findById(postId).orElseThrow();

        assertThat(updatedPost.getLikeCount()).isEqualTo(1);
        assertThat(postLikeRepository.existsByPostIdAndUserId(postId, userId)).isTrue();
    }

    @Test
    @Transactional
    void like_shouldThrowEntityNotFoundException_whenPostDoesNotExistAndWithNoFKConstrains() { // 의도된 예외 던지나 검증
        // given
        Long nonExistentPostId = 9999L;
        PostLikeRequest request = new PostLikeRequest(nonExistentPostId, 100L);

        // when & then
        assertThatThrownBy(() -> postService.like(request))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("Post not found."); // PostId는 보안상 메세징하지 않는 것이 좋다.
    }
}
