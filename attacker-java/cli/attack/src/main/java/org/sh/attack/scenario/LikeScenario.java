package org.sh.attack.scenario;

import java.net.URI;
import java.net.http.HttpRequest;
import org.sh.attack.config.AttackConfig;
import org.sh.attack.scenario.spec.AttackScenario;

public class LikeScenario implements AttackScenario {

  private final AttackConfig config;

  public LikeScenario(AttackConfig config) {
    this.config = config;
  }

  @Override
  public HttpRequest toRequest(int sequence) {
    int userPoolSize = 10000;
    int userId = (sequence % userPoolSize) + 1;

    String body = """
        {
          "postId": 1,
          "userId": %d
        }
        """.formatted(userId);

    return HttpRequest.newBuilder()
        .uri(URI.create(config.url() + "/post/like"))
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(body))
        .build();
  }
}