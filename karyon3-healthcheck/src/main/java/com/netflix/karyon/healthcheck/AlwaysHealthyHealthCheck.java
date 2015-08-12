package com.netflix.karyon.healthcheck;

import java.util.concurrent.CompletableFuture;

import javax.inject.Singleton;

@Singleton
public class AlwaysHealthyHealthCheck implements HealthCheck {
    @Override
    public CompletableFuture<HealthStatus> check() {
        return CompletableFuture.completedFuture(HealthStatuses.healthy());
    }
}
