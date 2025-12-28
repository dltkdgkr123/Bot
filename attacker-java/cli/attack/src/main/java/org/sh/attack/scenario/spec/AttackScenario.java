package org.sh.attack.scenario.spec;

import java.net.http.HttpRequest;

public interface AttackScenario {

  HttpRequest toRequest(int sequence);
}
