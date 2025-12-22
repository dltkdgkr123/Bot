package com.sh.bdt.repository;

import com.sh.bdt.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

  boolean existsByPostIdAndUserId(Long postId, Long userId);
}
