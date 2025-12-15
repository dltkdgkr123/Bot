package org.sh.attack.config;

public record AttackConfig(
    String url,
    int threads,
    int requestsPerThread,
    boolean burst,
    String scenario
) {
}
