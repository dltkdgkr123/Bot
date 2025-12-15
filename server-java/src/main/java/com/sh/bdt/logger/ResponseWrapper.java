package com.sh.bdt.logger;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.util.ContentCachingResponseWrapper;

@ConditionalOnProperty(
    name = "http.logging.enabled",
    havingValue = "true",
    matchIfMissing = false
)
public class ResponseWrapper extends ContentCachingResponseWrapper {

  public ResponseWrapper(HttpServletResponse response) {
    super(response);
  }
}
