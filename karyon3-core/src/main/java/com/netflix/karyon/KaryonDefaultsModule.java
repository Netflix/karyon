package com.netflix.karyon;

import com.netflix.governator.SingletonModule;
import com.netflix.karyon.api.health.HealthCheck;
import com.netflix.karyon.api.health.HealthIndicatorRegistry;
import com.netflix.karyon.api.lifecycle.ApplicationLifecycle;
import com.netflix.karyon.health.AllHealthIndicatorRegistry;
import com.netflix.karyon.health.HealthCheckImpl;
import com.netflix.karyon.lifecycle.LifecycleListenerApplicationLifecycle;

public final class KaryonDefaultsModule extends SingletonModule {
    @Override
    protected void configure() {
        bind(ApplicationLifecycle.class).to(LifecycleListenerApplicationLifecycle.class);
        bind(HealthIndicatorRegistry.class).to(AllHealthIndicatorRegistry.class);
        bind(HealthCheck.class).to(HealthCheckImpl.class);
    }
}
