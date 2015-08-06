package com.netflix.karyon.healthcheck;

import java.util.Map;

import com.google.inject.ImplementedBy;

@ImplementedBy(DefaultHealthCheckRegistry.class)
public interface HealthCheckRegistry {
    /**
     * Return a set of all known health checks
     * @return
     */
    Map<String, HealthCheck> getHealthChecks();
}
