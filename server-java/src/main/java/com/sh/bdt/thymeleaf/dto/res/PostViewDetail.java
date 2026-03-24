package com.sh.bdt.thymeleaf.dto.res;

import com.sh.bdt.entity.Post;

public record PostViewDetail(PostInfo PostInfo,
                             UserContext userContext) {

    public record PostInfo(
        Long postId,
        int likeCount
    ) {

    }

    public record UserContext(
        boolean isLogin,
        UserInfo userInfo) {

        public static UserContext from(Long userId, boolean hasLiked) {
            if (userId == null) {
                return new UserContext(false, null);
            }
            return new UserContext(true, new UserInfo(userId, hasLiked));
        }
    }

    public record UserInfo(
        Long userId,
        boolean hasLiked
    ) {

    }

    public static PostViewDetail of(Post post, Long userId, Long postLikeId) {
        return new PostViewDetail(
            new PostInfo(post.getId(), post.getLikeCount()),
            UserContext.from(userId, postLikeId != null)
        );
    }
}
