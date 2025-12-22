package org.sh.attack.factory;

import java.net.http.HttpRequest;
import org.sh.attack.config.AttackConfig;
import org.sh.attack.scenario.LikeScenario;
import org.sh.attack.scenario.PingScenario;

public class ScenarioRequestResolver {

  public static HttpRequest create(
      AttackConfig attackConfig) {

    String scenario = attackConfig.scenario();

    return switch (scenario) {
      case "ping" -> new PingScenario(attackConfig).toRequest();
      case "like" -> new LikeScenario(attackConfig).toRequest();
      default -> throw new IllegalArgumentException(
          "Unknown scenario: " + scenario
      );
    };
  }

}

