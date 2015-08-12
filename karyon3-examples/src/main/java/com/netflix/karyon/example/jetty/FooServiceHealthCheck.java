package com.netflix.karyon.example.jetty;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.netflix.karyon.healthcheck.HealthCheck;
import com.netflix.karyon.healthcheck.HealthStatus;
import com.netflix.karyon.healthcheck.HealthStatuses;

@Singleton
public class FooServiceHealthCheck implements HealthCheck {
    private FooService service;

    @Inject
    public FooServiceHealthCheck(FooService service) {
        this.service = service;
    }
    
    @Override
    public CompletableFuture<HealthStatus> check() {
        return CompletableFuture.supplyAsync(new Supplier<HealthStatus>() {
            @Override
            public HealthStatus get() {
                double errorRate = service.getErrorRate();
                Map<String, Object> attributes = new HashMap<>();
                attributes.put("errorRate", errorRate);
                
                if (service.getErrorRate() > 0.5) {
                    return HealthStatuses.unhealthy(attributes);
                }
                else {
                    return HealthStatuses.healthy(attributes);
                }
            }
        });
    }
}
