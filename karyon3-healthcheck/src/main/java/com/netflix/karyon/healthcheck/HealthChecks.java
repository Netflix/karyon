package com.netflix.karyon.healthcheck;

import java.util.concurrent.CompletableFuture;

public final class HealthChecks {
    public static HealthCheck alwaysHealthy() {
        return memoize(HealthStatuses.healthy());
    }
    
    public static HealthCheck alwaysUnhealthy() {
        return memoize(HealthStatuses.unhealthy(new Exception("Unhealthy")));
    }
    
    public static HealthCheck memoize(final HealthStatus status) {
        return new HealthCheck() {
            @Override
            public CompletableFuture<HealthStatus> check() {
                return CompletableFuture.completedFuture(status);
            }
        };
    }
}
