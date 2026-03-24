package com.sh.bdt.thymeleaf.dto.req;

public record PostViewDetailRequest(Long userId) {

    public PostViewDetailRequest() {
        this(null);
    }
}
