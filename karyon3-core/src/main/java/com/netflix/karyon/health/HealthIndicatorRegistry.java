package com.netflix.karyon.health;

import java.util.Arrays;
import java.util.List;

import com.google.inject.ImplementedBy;

/**
 * Registry of active HealthIndicator's.  The HealthIndicator are used by {@link HealthCheck} to 
 * determine the application health.
 * 
 * The default registry, {@link AllHealthIndicatorRegistry} uses Guice bindings to determine the
 * list of all active indicators.  To create a curated list of {@link HealthIndicator}s regardless of 
 * Guice bindings create a binding to HealthIndicatorRegistry as follows
 * 
 * <code>
 * {@literal @}Provides
 * {@literal @}Singleton
 * HealthIndicatorRegistry getHealthIndicatorRegistry({@literal @}Named("cpu") HealthIndicator cpuIndicator, {@literal @}Named("foo") HealthIndicator fooIndicator) {
 *    return HealthIndicatorRegistry.from(cpuIndicator, fooIndicator);
 * }
 * </code>
 * 
 * @author elandau
 */
@ImplementedBy(AllHealthIndicatorRegistry.class)
public interface HealthIndicatorRegistry {
    /**
     * @return Return a list of all active health checks
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
