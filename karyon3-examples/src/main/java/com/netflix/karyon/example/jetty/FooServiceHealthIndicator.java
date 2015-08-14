package com.netflix.karyon.example.jetty;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.netflix.karyon.healthcheck.HealthIndicator;
import com.netflix.karyon.healthcheck.HealthIndicatorStatus;
import com.netflix.karyon.healthcheck.HealthIndicatorStatuses;

@Singleton
public class FooServiceHealthIndicator implements HealthIndicator {
    private FooService service;

    @Inject
    public FooServiceHealthIndicator(FooService service) {
        this.service = service;
    }
    
    @Override
    public CompletableFuture<HealthIndicatorStatus> check() {
        return CompletableFuture.supplyAsync(new Supplier<HealthIndicatorStatus>() {
            @Override
            public HealthIndicatorStatus get() {
                double errorRate = service.getErrorRate();
                Map<String, Object> attributes = new HashMap<>();
                attributes.put("errorRate", errorRate);
                
                if (errorRate > 0.5) {
                    return HealthIndicatorStatuses.unhealthy(getName(), attributes);
                }
                else {
                    return HealthIndicatorStatuses.healthy(getName(), attributes);
                }
            }
        });
    }

    @Override
    public String getName() {
        return "foo";
    }
}
