package com.sh.bdt.thymeleaf.service;

import com.sh.bdt.thymeleaf.dto.req.PostViewDetailRequest;
import com.sh.bdt.thymeleaf.dto.res.PostViewDetail;
import com.sh.bdt.thymeleaf.repository.PostViewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostViewService {

    private final PostViewRepository postViewRepository;

    public PostViewDetail getPostViewDetail(Long postId, PostViewDetailRequest postViewDetailRequest) {
        PostViewDetail postViewDetail =
            postViewRepository.getPostViewDetail(postId, postViewDetailRequest);
        return postViewDetail;
    }
}
