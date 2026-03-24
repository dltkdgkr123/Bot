package com.sh.bdt.controller;

import com.sh.bdt.dto.req.PostLikeRequest;
import com.sh.bdt.dto.req.PostLikeRequestV2;
import com.sh.bdt.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/post")
@RestController
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping("/like")
    public ResponseEntity<Void> like(@RequestBody PostLikeRequest postLikeRequest) {
        // WARNING: get userId from request body(not token, ..), skip user auth validate for experiment
        postService.like(postLikeRequest);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/like", headers = "X-API-VERSION=2")
    public ResponseEntity<Void> likeV2(@RequestBody PostLikeRequestV2 postLikeRequestv2) {
        // WARNING: get userId from request body(not token, ..), skip user auth validate for experiment
        postService.likeV2(postLikeRequestv2);
        return ResponseEntity.ok().build();
    }
}
