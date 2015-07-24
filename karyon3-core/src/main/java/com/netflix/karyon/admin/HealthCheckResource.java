package com.netflix.karyon.admin;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.netflix.karyon.healthcheck.HealthCheckManager;
import com.netflix.karyon.healthcheck.HealthStatus;

/**
 * Resource the performs the healthcheck and returns the current result on every call
 * 
 * TODO: Throttle updates
 * @author elandau
 *
 */
@Singleton
public class HealthCheckResource {
    private final HealthCheckManager manager;

    @Inject
    public HealthCheckResource(HealthCheckManager manager) {
        this.manager = manager;
    }
    
    // Perform the actual health check
    public HealthStatus get() {
        return manager.check();
    }
}
