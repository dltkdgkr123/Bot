package com.sh.bdt.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/ping")
@RestController
public class PingController {

  @GetMapping
  ResponseEntity<String> ping() {
    return ResponseEntity.ok().body("PONG");
  }
}
