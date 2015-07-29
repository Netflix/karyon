package com.netflix.karyon.healthcheck;

import java.util.Map;

import com.google.inject.ImplementedBy;

/**
 * Policy for resolving multiple HealthCheckes into a HealthStatuses.
 * This policy may also be used to filter HealthChecks or implement 
 * caching.
 * 
 * @author elandau
 */
@ImplementedBy(DefaultHealthCheckResolver.class)
public interface HealthCheckResolver {
    /**
     * 
     * @param healthChecks
     * @return
     */
    Map<String, HealthStatus> check(Map<String, HealthCheck> healthChecks);
}
