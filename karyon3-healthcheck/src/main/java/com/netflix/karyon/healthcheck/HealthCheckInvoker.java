package com.netflix.karyon.healthcheck;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.google.inject.ImplementedBy;

/**
 * Policy for resolving multiple HealthCheckes into a HealthStatuses.
 * This policy may also be used to filter HealthChecks or implement 
 * caching.
 * 
 * @author elandau
 */
@ImplementedBy(DefaultHealthCheckInvoker.class)
public interface HealthCheckInvoker {
    /**
     * 
     * @param healthChecks
     * @return
     */
    CompletableFuture<Map<String, HealthStatus>> check(Map<String, HealthCheck> healthChecks);
}
