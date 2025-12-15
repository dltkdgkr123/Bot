package com.sh.bdt.logger;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@ConditionalOnProperty(
    name = "http.logging.enabled",
    havingValue = "true",
    matchIfMissing = false
)
@Component
    public class HttpLoggingFilter extends HttpFilter {

    private static final Logger log = LoggerFactory.getLogger(HttpLoggingFilter.class);

    @Override
    protected void doFilter(HttpServletRequest request,
        HttpServletResponse response,
        FilterChain chain) throws IOException, ServletException {

        log.info("=== HTTP REQUEST ===");
        log.info("Method: {}", request.getMethod());
        log.info("URI: {}", request.getRequestURI());
        log.info("QueryString: {}", request.getQueryString());
        log.info("Protocol: {}", request.getProtocol());
        log.info("RemoteAddr: {}", request.getRemoteAddr());

        // 요청 헤더 모두 출력
        Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames != null) {
            while (headerNames.hasMoreElements()) {
                String name = headerNames.nextElement();
                String value = request.getHeader(name);
                log.info("Header: {}={}", name, value);
            }
        }

        ResponseWrapper responseWrapper = new ResponseWrapper(response);

        chain.doFilter(request, responseWrapper);

        log.info("=== HTTP RESPONSE ===");
        log.info("Status: {}", responseWrapper.getStatus());
        responseWrapper.getHeaderNames().forEach(name ->
            log.info("Header: {}={}", name, responseWrapper.getHeader(name))
        );

        // 바디 로깅
        byte[] content = responseWrapper.getContentAsByteArray();
        if (content.length > 0) {
            String body = new String(content, response.getCharacterEncoding());
            log.info("Body: {}", body);
        }

        // 실제 Response로 복사
        responseWrapper.copyBodyToResponse();
    }
}
