package com.sh.bdt.ch02;

import static org.assertj.core.api.Assertions.assertThat;

import com.sh.bdt.dto.req.LikeRequest;
import com.sh.bdt.entity.Post;
import com.sh.bdt.repository.PostLikeRepository;
import com.sh.bdt.repository.PostRepository;
import com.sh.bdt.service.PostService;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Tag("ch02")
@SpringBootTest
class MVCTests {

  @Autowired
  PostService postService;

  @Autowired PostRepository postRepository;

  @Autowired PostLikeRepository postLikeRepository;

  @Test
  @Transactional
  void like_shouldIncreaseLikeCount_whenUserHasNotLiked() {

    // given
    Post post = Post.create();
    postRepository.save(post);

    Long postId = post.getId();
    Long userId = 100L;

    LikeRequest request = new LikeRequest(postId, userId);

    // when
      postService.like(request);

    // then
    Post updatedPost = postRepository.findById(postId).orElseThrow();

    assertThat(updatedPost.getLikeCount()).isEqualTo(1);
    assertThat(postLikeRepository.existsByPostIdAndUserId(postId, userId)).isTrue();
  }
}
