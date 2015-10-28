package com.netflix.karyon.health;

import java.util.concurrent.CompletableFuture;

import javax.inject.Singleton;

import com.netflix.governator.DefaultLifecycleListener;
import com.netflix.karyon.api.health.HealthIndicator;
import com.netflix.karyon.api.health.HealthIndicatorStatus;

@Singleton
final public class InjectorHealthIndicator extends DefaultLifecycleListener implements HealthIndicator {

    private volatile Throwable error;
    
    @Override
    public CompletableFuture<HealthIndicatorStatus> check() {
        return (error != null) 
            ? CompletableFuture.completedFuture(HealthIndicatorStatus.unhealthy(getName(), error))
            : CompletableFuture.completedFuture(HealthIndicatorStatus.healthy(getName()));
    }

    @Override
    public String getName() {
        return "guice";
    }

    @Override
    public void onStartFailed(Throwable t) {
        error = t;
    }

}
