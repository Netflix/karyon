package com.netflix.karyon.examples.hellonoss.server.jersey;

import com.netflix.adminresources.resources.KaryonWebAdminModule;
import com.netflix.governator.annotations.Modules;
import com.netflix.karyon.KaryonBootstrap;
import com.netflix.karyon.ShutdownModule;
import com.netflix.karyon.archaius.ArchaiusBootstrap;
import com.netflix.karyon.examples.hellonoss.common.LoggingInterceptor;
import com.netflix.karyon.examples.hellonoss.common.auth.AuthInterceptor;
import com.netflix.karyon.examples.hellonoss.common.auth.AuthenticationService;
import com.netflix.karyon.examples.hellonoss.common.auth.AuthenticationServiceImpl;
import com.netflix.karyon.examples.hellonoss.common.health.HealthCheck;
import com.netflix.karyon.examples.hellonoss.server.jersey.JerseyHelloWorldApp.KaryonJerseyModuleImpl;
import com.netflix.karyon.jersey.blocking.KaryonJerseyModule;

@ArchaiusBootstrap
@KaryonBootstrap(name = "hello-netflix-oss", healthcheck = HealthCheck.class)
@Modules(include = {
        ShutdownModule.class,
        KaryonWebAdminModule.class,
        // KaryonEurekaModule.class, // Uncomment this to enable Eureka client.
        KaryonJerseyModuleImpl.class
})
public interface JerseyHelloWorldApp {

    class KaryonJerseyModuleImpl extends KaryonJerseyModule {
        @Override
        protected void configureServer() {
            bind(AuthenticationService.class).to(AuthenticationServiceImpl.class);
            interceptorSupport().forUri("/*").intercept(LoggingInterceptor.class);
            interceptorSupport().forUri("/hello").interceptIn(AuthInterceptor.class);
            server().port(8888).threadPoolSize(100);
        }
    }
}
