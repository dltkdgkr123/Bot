package com.sh.bdt.api;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CH01Tests {

  @LocalServerPort int port;

  @Test
  void ping() {

    // given (+ then)
    HttpClient client =
        assertDoesNotThrow(
            HttpClient::newHttpClient, "HttpClient creation should not throw an exception");

    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:" + port + "/ping"))
            .GET()
            .build();

    // when (+ then)
    HttpResponse<String> response =
        assertDoesNotThrow(
            () -> {
              assertNotNull(client, "HttpClient instance should not be null");
              return client.send(request, HttpResponse.BodyHandlers.ofString());
            },
            "HttpClient.send() should complete without throwing an exception");

    // then
    assertNotNull(response);
    assertEquals(200, response.statusCode(), "Response status code should be 200");
    assertNotNull(response.body(), "Response body should not be null");

    log.info("status = {}", response.statusCode());
    log.info("body   = {}", response.body());
  }
}
