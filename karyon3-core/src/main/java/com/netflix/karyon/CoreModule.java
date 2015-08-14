package com.netflix.karyon;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.netflix.karyon.healthcheck.HealthIndicator;

public class CoreModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(HealthIndicator.class).annotatedWith(Names.named("guice")).to(InjectorHealthIndicator.class);
    }
}
