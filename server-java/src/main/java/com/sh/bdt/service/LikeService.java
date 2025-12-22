package com.sh.bdt.service;

import com.sh.bdt.dto.req.LikeRequest;
import com.sh.bdt.entity.Post;
import com.sh.bdt.entity.PostLike;
import com.sh.bdt.repository.PostLikeRepository;
import com.sh.bdt.repository.PostRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LikeService {

  private final PostRepository postRepository;
  private final PostLikeRepository postLikeRepository;

  @Transactional
  public void like(LikeRequest likeRequest) {

    Long postId = likeRequest.postId();
    Long userId = likeRequest.userId();

    Post post = postRepository.findById(postId).orElseThrow(); // s-lock

    if (postLikeRepository.existsByPostIdAndUserId(postId, userId)) {
      return;
    }

    postLikeRepository.save(new PostLike(post, userId));

    post.increaseLike();
  }
}
