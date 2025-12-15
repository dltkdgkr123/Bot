package org.sh.attack.parser;

import java.util.HashMap;
import java.util.Map;
import org.sh.attack.config.AttackConfig;
import org.sh.attack.scenario.enumeration.ScenarioEnum;

public class cliParser {

  public static AttackConfig parse(String[] args) {
    Map<String, String> map = parseArgs(args);

    String scenario = validateScenario(require(map, "--scenario"));

    String url = require(map, "--url");
    int threads = parseInt(map, "--threads", 1);
    int rpt = parseInt(map, "--rpt", 1);
    boolean burst = parseBoolean(map, "--burst", false);

    return new AttackConfig(
        url,
        threads,
        rpt,
        burst,
        scenario
    );
  }

  private static Map<String, String> parseArgs(String[] args) {
    Map<String, String> map = new HashMap<>();

    for (int i = 0; i < args.length - 1; i += 2) {
      map.put(args[i], args[i + 1]);
    }
    return map;
  }

  private static String validateScenario(String scenario) {

    return ScenarioEnum.from(scenario).toString();
  }

  private static String require(Map<String, String> map, String key) {
    String value = map.get(key);
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("Missing required argument: " + key);
    }
    return value;
  }

  private static int parseInt(Map<String, String> map, String key, int defaultValue) {
    return map.containsKey(key)
        ? Integer.parseInt(map.get(key))
        : defaultValue;
  }

  private static boolean parseBoolean(Map<String, String> map, String key, boolean defaultValue) {
    return map.containsKey(key)
        ? Boolean.parseBoolean(map.get(key))
        : defaultValue;
  }
}
