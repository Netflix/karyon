package com.netflix.karyon.healthcheck;

import com.google.inject.AbstractModule;

public class HealthCheckModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(HealthCheckRegistry.class).to(DefaultHealthCheckRegistry.class);
        bind(HealthCheckResolver.class).to(DefaultHealthCheckResolver.class);
        bind(HealthCheckAggregator.class).to(DefaultHealthCheckAggregator.class);
    }
}
