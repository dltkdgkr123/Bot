package org.sh.attack;

import org.sh.attack.config.AttackConfig;
import org.sh.attack.parser.cliParser;
import org.sh.attack.runner.AttackRunner;

public class Main {

  public static void main(String[] args) {
    AttackConfig config = cliParser.parse(args);
    AttackRunner runner = new AttackRunner(config);

    runner.run();
  }
}
