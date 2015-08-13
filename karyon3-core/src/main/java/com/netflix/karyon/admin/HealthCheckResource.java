package com.netflix.karyon.admin;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.netflix.karyon.healthcheck.HealthCheck;
import com.netflix.karyon.healthcheck.HealthStatus;

/**
 * Admin resource to access all health checks. 
 * 
 * TODO: Throttle updates
 * @author elandau
 *
 */
@Singleton
public class HealthCheckResource {
    private final HealthCheck healthcheck;

    @Inject
    public HealthCheckResource(HealthCheck healthcheck) {
        this.healthcheck = healthcheck;
    }
    
    // Perform the actual health check
    public HealthStatus get() {
        return healthcheck.check().join();
    }
}
