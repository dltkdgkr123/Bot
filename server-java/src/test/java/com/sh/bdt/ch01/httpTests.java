package com.sh.bdt.ch01;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@Tag("ch01")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class httpTests {

    @LocalServerPort
    int port;

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
    }
}
