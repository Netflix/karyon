package com.netflix.karyon;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Singleton;

import com.google.inject.Inject;
import com.netflix.karyon.healthcheck.HealthIndicator;
import com.netflix.karyon.healthcheck.HealthIndicatorRegistry;
import com.netflix.karyon.healthcheck.HealthIndicatorStatus;

/**
 * Determine a the health of an application by combining the ApplicationLifecycle state
 * with the health checks. 
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
        if (indicators.isEmpty()) {
            return CompletableFuture.completedFuture(HealthCheckStatus.healthy(lifecycle.getState()));
        }
        
        // Run all the HealthIndicators and collect the statuses.
        final List<HealthIndicatorStatus> statuses = new CopyOnWriteArrayList<>();
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
        
        return future.thenApply((t) -> {
            boolean isHealthy = calcIsHealth(t);
            LifecycleState state;
            switch (lifecycle.getState()) {
            case Starting:
            case Running:
                state = isHealthy
                      ? lifecycle.getState()
                      : LifecycleState.Failed;
                break;
                
            case Stopped:
            case Failed:
            default:
                state = lifecycle.getState();
                break;
            }
            return new HealthCheckStatus(state, t);
        });
    }
    
    private boolean calcIsHealth(List<HealthIndicatorStatus> statuses) {
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
