package org.sh.attack.scenario;

import java.net.URI;
import java.net.http.HttpRequest;
import org.sh.attack.config.AttackConfig;
import org.sh.attack.scenario.spec.AttackScenario;

public class LikeV2MassiveUserScenario implements AttackScenario {

  private final AttackConfig config;

  public LikeV2MassiveUserScenario(AttackConfig config) {
    this.config = config;
  }

  @Override
  public HttpRequest toRequest(int sequence) {
    int userPoolSize = 1000000;
    int userId = (sequence % userPoolSize) + 1;

    String body = """
        {
          "postId": 1,
          "userId": %d,
          "status": 1
        }
        """.formatted(userId);

    return HttpRequest.newBuilder()
        .uri(URI.create(config.url() + "/post/like"))
        .header("Content-Type", "application/json")
        .header("X-API-VERSION", "2")
        .POST(HttpRequest.BodyPublishers.ofString(body))
        .build();
  }
}