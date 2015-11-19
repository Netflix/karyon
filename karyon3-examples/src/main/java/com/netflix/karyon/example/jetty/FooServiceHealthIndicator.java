package com.netflix.karyon.example.jetty;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.netflix.karyon.api.health.HealthIndicatorStatus;
import com.netflix.karyon.spi.health.AbstractHealthIndicator;

@Singleton
public class FooServiceHealthIndicator extends AbstractHealthIndicator {
    private FooService service;

    @Inject
    public FooServiceHealthIndicator(FooService service) {
        super("foo");
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
                    return unhealthy(attributes);
                }
                else {
                    return healthy(attributes);
                }
            }
        });
    }

}
