package com.netflix.karyon;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.netflix.karyon.admin.InjectorHealthCheck;
import com.netflix.karyon.healthcheck.HealthCheck;
import com.netflix.karyon.healthcheck.HealthCheckModule;

public class CoreModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new HealthCheckModule());
        
        bind(HealthCheck.class).annotatedWith(Names.named("guice")).to(InjectorHealthCheck.class);
    }

}
