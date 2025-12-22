package com.sh.bdt.controller;

import com.sh.bdt.dto.req.LikeRequest;
import com.sh.bdt.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/post")
@RestController
@RequiredArgsConstructor
public class LikeController {

  private final LikeService likeService;

  @PostMapping("/like")
  public ResponseEntity<Void> like(@RequestBody LikeRequest likeRequest) {
    // skip user auth validate
    likeService.like(likeRequest);
    return ResponseEntity.ok().build();
  }
}
