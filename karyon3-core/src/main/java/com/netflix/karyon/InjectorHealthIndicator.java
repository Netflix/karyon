package com.netflix.karyon;

import java.util.concurrent.CompletableFuture;

import javax.inject.Singleton;

import com.netflix.governator.DefaultLifecycleListener;
import com.netflix.karyon.healthcheck.HealthIndicator;
import com.netflix.karyon.healthcheck.HealthIndicatorStatus;
import com.netflix.karyon.healthcheck.HealthIndicatorStatuses;

@Singleton
public class InjectorHealthIndicator extends DefaultLifecycleListener implements HealthIndicator {

    private volatile Throwable error;
    
    @Override
    public CompletableFuture<HealthIndicatorStatus> check() {
        return (error != null) 
            ? CompletableFuture.completedFuture(HealthIndicatorStatuses.unhealthy(getName(), error))
            : CompletableFuture.completedFuture(HealthIndicatorStatuses.healthy(getName()));
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
