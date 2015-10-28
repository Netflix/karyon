package com.netflix.karyon;

import com.google.inject.name.Names;
import com.netflix.governator.DefaultModule;
import com.netflix.karyon.api.health.HealthIndicator;
import com.netflix.karyon.health.InjectorHealthIndicator;

public class CoreModule extends DefaultModule {
    @Override
    protected void configure() {
        bind(HealthIndicator.class).annotatedWith(Names.named("guice")).to(InjectorHealthIndicator.class).asEagerSingleton();
    }
}
