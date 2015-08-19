package com.netflix.karyon.health;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Singleton;

import com.google.inject.Inject;
import com.netflix.karyon.lifecycle.ApplicationLifecycle;

/**
 * Determine the health of an application by combining the ApplicationLifecycle state
 * with the {@link HealthIndicator}s tracked by the {@link HealthIndicatorRegistry}.   
 * 
 * @author elandau
 */
@Singleton
public class HealthCheck {
    private final HealthIndicatorRegistry registry;
    private final ApplicationLifecycle lifecycle;
    
    @Inject
    public HealthCheck(ApplicationLifecycle lifecycle, HealthIndicatorRegistry registry) {
        this.registry = registry;
        this.lifecycle = lifecycle;
    }
    
    public CompletableFuture<HealthCheckStatus> check() {
        final CompletableFuture<List<HealthIndicatorStatus>> future = new CompletableFuture<>();
        
        final List<HealthIndicator> indicators = registry.getHealthIndicators();
        final List<HealthIndicatorStatus> statuses = new CopyOnWriteArrayList<>();
        if (indicators.isEmpty()) {
            future.complete(statuses);
        }
        else {
            // Run all the HealthIndicators and collect the statuses.
            final AtomicInteger counter = new AtomicInteger(indicators.size());
            for (HealthIndicator indicator : indicators) {
                indicator.check().thenAccept((result) -> {
                    // Aggregate the health checks
                    statuses.add(result);
                    
                    // Reached the last health check so complete the future
                    if (counter.decrementAndGet() == 0) {
                        future.complete(statuses);
                    }
                });
            }
        }
        
        return future.thenApply((t) -> {
            HealthState state;
            if (!calcIsHealthy(t)) {
                state = HealthState.Unhealthy;
            }
            else {
                switch (lifecycle.getState()) {
                case Starting:
                    state = HealthState.Starting;
                    break;
                  
                case Running:
                    state = HealthState.Healthy;
                    break;
                    
                case Stopping:
                case Stopped:
                default:
                    state = HealthState.OutOfService;
                    break;
                }
            }
            return new HealthCheckStatus(state, t);
        });
    }
    
    /**
     * Return false is any of the health indicators are unhealthy.
     * @param statuses
     * @return
     */
    private boolean calcIsHealthy(List<HealthIndicatorStatus> statuses) {
        for (HealthIndicatorStatus status : statuses) {
            if (!status.isHealthy()) {
                return false;
            }
        }
        return true;
    }

    public String getName() {
        return "root";
    }
}
