package com.netflix.karyon.healthcheck;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.inject.Singleton;

import com.google.inject.Inject;

/**
 * {@link HealthCheck} that is derived from multiple health checks.  The
 * default implementation returns the worst health status from any 
 * health checks registered via the {@link HealthCheckRegistry}.  It is however
 * possible to manually construct the CompositeHealthCheck form a subset
 * of known healthchecks as an opt in way to compose multiple health checks.
 * 
 * To use the default implementation add the following binding,
 * <pre>
 * {@code 
 * bind(HealthCheck.class).to(CompositeHealthCheck.class);
 * }
 * </pre>
 * 
 * To use a known subset add the following @Provides, 
 * 
 * <pre>
 * {@code
 * @Provides
 * @Singleton
 * HealthCheck getHealthCheck(@Named("foo") fooHc, @Named("bar") barHc) {
 *     return new CompositeHealthCheck(ImmutableSet.of(fooHc, barHc));
 * }
 * }
 * </pre>
 * 
 * @author elandau
 *
 */
@Singleton
public class CompositeHealthCheck implements HealthCheck {
    private final HealthCheckAggregator aggregator;
    private final HealthCheckInvoker resolver;
    private final HealthCheckRegistry registry;

    public CompositeHealthCheck(Map<String, HealthCheck> healthChecks) {
        this(
            new HealthCheckRegistry() {
                @Override
                public Map<String, HealthCheck> getHealthChecks() {
                    return healthChecks;
                }
            }, 
            null, 
            new DefaultHealthCheckAggregator());     
    }
    
    @Inject
    public CompositeHealthCheck(HealthCheckRegistry registry, HealthCheckInvoker resolver, HealthCheckAggregator aggregator) {
        this.registry = registry;
        
        this.resolver = resolver == null 
                ? new DefaultHealthCheckInvoker()
                : resolver;
        this.aggregator = aggregator == null 
                ? new DefaultHealthCheckAggregator()
                : aggregator;
    }
    
    @Override
    public CompletableFuture<HealthStatus> check() {
        return resolver
            .check(registry.getHealthChecks())
            .thenApply((statuses) -> aggregator.aggregate(statuses));
    }
}
