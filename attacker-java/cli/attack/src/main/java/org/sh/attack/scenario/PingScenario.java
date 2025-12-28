package org.sh.attack.scenario;

import java.net.URI;
import java.net.http.HttpRequest;
import org.sh.attack.config.AttackConfig;
import org.sh.attack.scenario.spec.AttackScenario;

public class PingScenario implements AttackScenario {

  private final AttackConfig config;

  public PingScenario(AttackConfig config) {
    this.config = config;
  }

  @Override
  public HttpRequest toRequest(int sequence) {
    // ignore sequence
    return HttpRequest.newBuilder()
        .uri(URI.create(config.url() + "/ping"))
        .header("Content-Type", "application/json")
        .GET()
        .build();
  }
}
