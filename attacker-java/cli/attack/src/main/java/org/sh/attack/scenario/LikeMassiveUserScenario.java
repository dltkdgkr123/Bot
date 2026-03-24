package org.sh.attack.scenario;

import java.net.URI;
import java.net.http.HttpRequest;
import org.sh.attack.config.AttackConfig;
import org.sh.attack.scenario.spec.AttackScenario;

public class LikeMassiveUserScenario implements AttackScenario {

  private final AttackConfig config;

  public LikeMassiveUserScenario(AttackConfig config) {
    this.config = config;
  }

  @Override
  public HttpRequest toRequest(int sequence) {
    int userPoolSize = 100000;
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