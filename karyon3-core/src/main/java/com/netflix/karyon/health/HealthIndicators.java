package com.netflix.karyon.health;

import java.util.concurrent.CompletableFuture;

public final class HealthIndicators {
    public static HealthIndicator alwaysHealthy(String name) {
        return memoize(name, HealthIndicatorStatuses.healthy(name));
    }
    
    public static HealthIndicator alwaysUnhealthy(String name) {
        return memoize(name, HealthIndicatorStatuses.unhealthy(name, new Exception("Unhealthy")));
    }
    
    public static HealthIndicator memoize(final String name, final HealthIndicatorStatus status) {
        return new HealthIndicator() {
            @Override
            public CompletableFuture<HealthIndicatorStatus> check() {
                return CompletableFuture.completedFuture(status);
            }

            @Override
            public String getName() {
                return name;
            }
        };
    }
}
