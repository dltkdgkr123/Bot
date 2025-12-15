package org.sh.attack.scenario;

import java.net.URI;
import java.net.http.HttpRequest;
import org.sh.attack.config.AttackConfig;
import org.sh.attack.scenario.spec.AttackScenario;

// ch01: Stateless Ping baseline
public class PingScenario implements AttackScenario {

  AttackConfig config;

  public PingScenario(AttackConfig config) {
    this.config = config;
  }

  @Override
  public HttpRequest toRequest() {

    return HttpRequest.newBuilder()
        .uri(URI.create(config.url()))
        .header("Content-Type", "application/json")
        .GET()
        .build();
  }
}
