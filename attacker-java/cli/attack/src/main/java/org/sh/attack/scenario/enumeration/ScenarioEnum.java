package org.sh.attack.scenario.enumeration;

import java.util.Arrays;

public enum ScenarioEnum {

  PING("ping");

  private final String id;

  ScenarioEnum(String id) {
    this.id = id;
  }

  @Override
  public String toString() {
    return id;
  }

  public static ScenarioEnum from(String value) {
    return Arrays.stream(values())
        .filter(s -> s.id.equalsIgnoreCase(value))
        .findFirst()
        .orElseThrow(() ->
            new IllegalArgumentException("Unknown scenario: " + value)
        );
  }
}
