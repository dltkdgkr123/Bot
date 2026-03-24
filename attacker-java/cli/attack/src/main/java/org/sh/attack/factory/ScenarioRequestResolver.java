package org.sh.attack.factory;

import org.sh.attack.config.AttackConfig;
import org.sh.attack.scenario.LikeMassiveUserScenario;
import org.sh.attack.scenario.LikeScenario;
import org.sh.attack.scenario.LikeV2MassiveUserScenario;
import org.sh.attack.scenario.LikeV2Scenario;
import org.sh.attack.scenario.PingScenario;
import org.sh.attack.scenario.spec.AttackScenario;

public class ScenarioRequestResolver {

  public static AttackScenario resolve(
      AttackConfig attackConfig) {

    String scenario = attackConfig.scenario();

    return switch (scenario) {
      case "ping" -> new PingScenario(attackConfig);
      case "like" -> new LikeScenario(attackConfig);
      case "like_massive_users" -> new LikeMassiveUserScenario(attackConfig);
      case "like_v2" -> new LikeV2Scenario(attackConfig);
      case "like_v2_massive_users" -> new LikeV2MassiveUserScenario(attackConfig);
      default -> throw new IllegalArgumentException(
          "Unknown scenario: " + scenario
      );
    };
  }

}

