package com.netflix.karyon.health;

import java.util.Collections;
import java.util.List;

import javax.inject.Singleton;

import com.netflix.karyon.api.health.HealthIndicator;
import com.netflix.karyon.api.health.HealthIndicatorRegistry;

/**
 * HealthIndicatorRegistry to use when no health check indicators are desired.
 * HealthCheck should always be true
 */
@Singleton
final public class EmptyHealthIndicatorRegistry implements HealthIndicatorRegistry {
    @Override
    public List<HealthIndicator> getHealthIndicators() {
        return Collections.emptyList();
    }
}
