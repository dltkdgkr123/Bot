package com.sh.bdt.thymeleaf.repository.querydsl;

import static com.sh.bdt.entity.QPost.post;
import static com.sh.bdt.entity.QPostLike.postLike;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sh.bdt.thymeleaf.dto.req.PostViewDetailRequest;
import com.sh.bdt.thymeleaf.dto.res.PostViewDetail;
import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PostViewRepositoryImpl implements PostViewRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public PostViewDetail getPostViewDetail(Long postId, PostViewDetailRequest request) {

        Long userId = request.userId();

        // 1. 공통 쿼리 작성
        var query = queryFactory
            .from(post)
            .where(post.id.eq(postId));

        Expression<Long> postLikeIdExpression;

        // 2. userId가 있을 때만 join 및 select문 update
        if (userId != null) {
            query.leftJoin(postLike).on(
                postLike.post.id.eq(postId),
                postLike.userId.eq(userId)
            );
            postLikeIdExpression = postLike.id; // 실제 id를 가져오는 표현식으로 변경
        } else {
            postLikeIdExpression = Expressions.asNumber(Expressions.nullExpression());
        }

        var result = Optional.ofNullable(
                query
                    .select(post, postLikeIdExpression)
                    .limit(1).fetchOne()
            )
            .orElseThrow(() -> new EntityNotFoundException("Post Not Found."));

        return PostViewDetail.of(
            result.get(post),
            userId,
            result.get(postLikeIdExpression)
        );
    }

}
