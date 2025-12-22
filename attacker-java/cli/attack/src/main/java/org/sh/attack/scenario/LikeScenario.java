package org.sh.attack.scenario;

import java.net.URI;
import java.net.http.HttpRequest;
import org.sh.attack.config.AttackConfig;
import org.sh.attack.scenario.spec.AttackScenario;

// ch02: DB-Write baseline
public class LikeScenario implements AttackScenario {

  AttackConfig config;

  public LikeScenario(AttackConfig config) {
    this.config = config;
  }

  @Override
  public HttpRequest toRequest() {

    String body = """
        { "postId": 1,
         "userId": 1}
        """;

    return HttpRequest.newBuilder()
        .uri(URI.create(config.url()))
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(body))
        .build();
  }
}
