package com.sh.bdt.repository;

import com.sh.bdt.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    boolean existsByPostIdAndUserId(Long postId, Long userId);

    @Modifying(clearAutomatically = true)
    @Query(value = "insert ignore into post_like (post_id, user_id, created_at, updated_at) "
        + "values (:postId, :userId, now(), now())", nativeQuery = true)
    int insertIgnore(@Param("postId") Long postId, @Param("userId") Long userId);
}
