package com.netflix.karyon.healthcheck;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class HealthCheckManager {
    private final HealthCheckAggregator aggregator;
    private final HealthCheckResolver resolver;
    private final HealthCheckRegistry registry;

    @Inject
    public HealthCheckManager(HealthCheckRegistry registry, HealthCheckResolver resolver, HealthCheckAggregator aggregator) {
        this.registry = registry;
        this.resolver = resolver;
        this.aggregator = aggregator;
    }
    
    public HealthStatus check() {
        return aggregator.aggregate(resolver.check(registry.getHealthChecks()));
    }
}
