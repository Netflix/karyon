package com.netflix.karyon.healthcheck;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Default implementation for HealthCheckResolver that simply calls all the available HealthChecks.
 * The HealthCheck results are aggregated asynchronously and the future completes only after all 
 * HealthChecks have reported their status.
 * 
 * @author elandau
 *
 */
public class DefaultHealthCheckInvoker implements HealthCheckInvoker {
    @Override
    public CompletableFuture<Map<String, HealthStatus>> check(Map<String, HealthCheck> healthChecks) {
        final CompletableFuture<Map<String, HealthStatus>> future = new CompletableFuture<>();
        
        final Map<String, HealthStatus> statuses = new ConcurrentHashMap<>();
        final AtomicInteger counter = new AtomicInteger(healthChecks.size());
        for (Entry<String, HealthCheck> entry : healthChecks.entrySet()) {
            entry.getValue().check().thenAccept((result) -> {
                // Aggregate the health checks
                statuses.put(entry.getKey(), result);
                
                // Reached the last health check so complete the future
                if (counter.decrementAndGet() == 0) {
                    future.complete(statuses);
                }
            });
        }
        
        return future;
    }
}
