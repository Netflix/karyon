package com.netflix.karyon.healthcheck;

import java.util.Map;
import java.util.stream.Collectors;

public class DefaultHealthCheckResolver implements HealthCheckResolver {
    @Override
    public Map<String, HealthStatus> check(Map<String, HealthCheck> healthChecks) {
        return healthChecks.entrySet().stream().collect(Collectors.toMap(
                e -> e.getKey(),
                e -> e.getValue().check()));
    }
}
