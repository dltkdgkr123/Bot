package com.sh.bdt.expection;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 409
    @ExceptionHandler(LikeConflictException.class)
    public ResponseEntity<String> handleLikeConflict(LikeConflictException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body("Conflict");
    }
}
