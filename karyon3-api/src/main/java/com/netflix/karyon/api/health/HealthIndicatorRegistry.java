package com.netflix.karyon.api.health;

import java.util.Arrays;
import java.util.List;

/**
 * Registry of active HealthIndicator's.  The HealthIndicator are used by {@link HealthCheck} to 
 * determine the application health.
 */
public interface HealthIndicatorRegistry {
    /**
     * Return a list of all active health checks
     * @return
     */
    List<HealthIndicator> getHealthIndicators();
    
    public static HealthIndicatorRegistry from(List<HealthIndicator> healthChecks) {
        return new HealthIndicatorRegistry() {
                @Override
                public List<HealthIndicator> getHealthIndicators() {
                    return healthChecks;
                }
            };     
    }

    public static HealthIndicatorRegistry from(HealthIndicator... healthChecks) {
        return from(Arrays.asList(healthChecks));
    }
}
