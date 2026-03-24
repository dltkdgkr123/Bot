package org.sh.attack.scenario;

import java.net.URI;
import java.net.http.HttpRequest;
import org.sh.attack.config.AttackConfig;
import org.sh.attack.scenario.spec.AttackScenario;

public class LikeV2Scenario implements AttackScenario {

  private final AttackConfig config;

  public LikeV2Scenario(AttackConfig config) {
    this.config = config;
  }

  @Override
  public HttpRequest toRequest(int sequence) {
    // ignore sequence
    String body = """
        {
          "postId": 1,
          "userId": 1,
          "status": 1
        }
        """;

    return HttpRequest.newBuilder()
        .uri(URI.create(config.url() + "/post/like"))
        .header("Content-Type", "application/json")
        .header("X-API-VERSION", "2")
        .POST(HttpRequest.BodyPublishers.ofString(body))
        .build();
  }
}