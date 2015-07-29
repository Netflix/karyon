package com.netflix.karyon.admin;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.netflix.karyon.healthcheck.HealthCheckRegistry;
import com.netflix.karyon.healthcheck.HealthCheckResolver;
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
    private final HealthCheckRegistry registry;
    private HealthCheckResolver resolver;

    @Inject
    public HealthCheckResource(HealthCheckRegistry registry, HealthCheckResolver resolver) {
        this.registry = registry;
        this.resolver = resolver;
    }
    
    // Perform the actual health check
    public Map<String, HealthStatus> get() {
        return resolver.check(registry.getHealthChecks());
    }
    
    public HealthStatus get(String hcName) {
        return registry.getHealthChecks().get(hcName).check();
    }
}
