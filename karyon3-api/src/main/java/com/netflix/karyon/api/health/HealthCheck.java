package com.netflix.karyon.api.health;

import java.util.concurrent.CompletableFuture;

/**
 * Contract for the top level application health check.  This is the HealthCheck
 * to be consulted when exposing a HealthCheck endpoint or for use when registering
 * with a service registry service.
 */
public interface HealthCheck {
    /**
     * Perform a health check.
     * @return A future on which the health check result will be emitted
     */
    CompletableFuture<HealthCheckStatus> check();
}
