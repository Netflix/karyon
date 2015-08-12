package com.netflix.karyon.example.jetty;

import java.util.concurrent.CompletableFuture;

import com.netflix.karyon.healthcheck.HealthCheck;
import com.netflix.karyon.healthcheck.HealthStatus;
import com.netflix.karyon.healthcheck.HealthStatuses;

public class MyHealthCheck implements HealthCheck {
    @Override
    public CompletableFuture<HealthStatus> check() {
        return CompletableFuture.completedFuture(HealthStatuses.healthy());
    }
}
