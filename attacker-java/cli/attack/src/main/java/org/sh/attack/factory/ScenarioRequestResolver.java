package org.sh.attack.factory;

import org.sh.attack.config.AttackConfig;
import org.sh.attack.scenario.LikeScenario;
import org.sh.attack.scenario.PingScenario;
import org.sh.attack.scenario.spec.AttackScenario;

public class ScenarioRequestResolver {

  public static AttackScenario resolve(
      AttackConfig attackConfig) {

    String scenario = attackConfig.scenario();

    return switch (scenario) {
      case "ping" -> new PingScenario(attackConfig);
      case "like" -> new LikeScenario(attackConfig);
      default -> throw new IllegalArgumentException(
          "Unknown scenario: " + scenario
      );
    };
  }

}

