package com.sh.bdt.repository;

import com.sh.bdt.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    @Modifying(clearAutomatically = true)
    @Query(value = "update post set like_count = like_count + 1, updated_at = now() "
        + "where id = :postId", nativeQuery = true)
    int increaseAtomicLikeCount(@Param("postId") Long postId);

    @Modifying(clearAutomatically = true)
    @Query(value = "update post set like_count = like_count - 1, updated_at = now() "
        + "where id = :postId", nativeQuery = true)
    int decreaseAtomicLikeCount(@Param("postId") Long postId);
}
