package com.netflix.karyon.admin;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.netflix.karyon.health.HealthCheck;
import com.netflix.karyon.health.HealthCheckStatus;

/**
 * Admin resource to access all health checks. 
 * 
 * TODO: Throttle updates
 * @author elandau
 *
 */
@Singleton
public class HealthCheckResource {
    private final HealthCheck healthCheck;

    @Inject
    public HealthCheckResource(HealthCheck healthCheck) {
        this.healthCheck = healthCheck;
    }

    public HealthCheckStatus get() {
        return healthCheck.check().join();
    }

}
