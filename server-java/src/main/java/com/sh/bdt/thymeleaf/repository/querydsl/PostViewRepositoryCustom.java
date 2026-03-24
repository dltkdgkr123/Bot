package com.sh.bdt.thymeleaf.repository.querydsl;

import com.sh.bdt.thymeleaf.dto.req.PostViewDetailRequest;
import com.sh.bdt.thymeleaf.dto.res.PostViewDetail;

public interface PostViewRepositoryCustom {

    PostViewDetail getPostViewDetail(Long postId, PostViewDetailRequest postViewDetailRequest);
}
