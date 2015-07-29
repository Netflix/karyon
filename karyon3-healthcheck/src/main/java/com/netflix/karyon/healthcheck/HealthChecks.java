package com.netflix.karyon.healthcheck;

public final class HealthChecks {
    public static HealthCheck alwaysHealthy() {
        return new HealthCheck() {
            @Override
            public HealthStatus check() {
                return HealthStatuses.healthy();
            }
        };
    }
    
    public static HealthCheck alwaysUnhealthy() {
        return new HealthCheck() {
            @Override
            public HealthStatus check() {
                return HealthStatuses.unhealthy(new Exception("Unhealthy"));
            }
        };
    }
    
    public static HealthCheck memoize(final HealthStatus status) {
        return new HealthCheck() {
            @Override
            public HealthStatus check() {
                return status;
            }
        };
    }
}
