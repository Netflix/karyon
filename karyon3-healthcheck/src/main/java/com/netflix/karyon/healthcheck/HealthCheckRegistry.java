package com.netflix.karyon.healthcheck;

import java.util.Map;

public interface HealthCheckRegistry {
    /**
     * Return a set of all known health checks
     * @return
     */
    Map<String, HealthCheck> getHealthChecks();
}
