package com.netflix.karyon.examples.hellonoss.server.simple.annotation;

import com.google.inject.Singleton;
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
import com.netflix.karyon.examples.hellonoss.server.simple.SimpleRouter;
import com.netflix.karyon.examples.hellonoss.server.simple.annotation.SimpleRoutingApp.KaryonRxRouterModuleImpl;
import com.netflix.karyon.servo.KaryonServoModule;
import com.netflix.karyon.transport.http.KaryonHttpModule;
import io.netty.buffer.ByteBuf;

/**
 * @author Tomasz Bak
 */
@ArchaiusBootstrap
@KaryonBootstrap(name = "hello-netflix-oss", healthcheck = HealthCheck.class)
@Singleton
@Modules(include = {
        ShutdownModule.class,
        KaryonServoModule.class,
        KaryonWebAdminModule.class,
        // KaryonEurekaModule.class, // Uncomment this to enable Eureka client.
        KaryonRxRouterModuleImpl.class
})
public interface SimpleRoutingApp {

    class KaryonRxRouterModuleImpl extends KaryonHttpModule<ByteBuf, ByteBuf> {

        public KaryonRxRouterModuleImpl() {
            super("httpServerA", ByteBuf.class, ByteBuf.class);
        }

        @Override
        protected void configureServer() {
            bindRouter().toInstance(new SimpleRouter());

            bind(AuthenticationService.class).to(AuthenticationServiceImpl.class);
            interceptorSupport().forUri("/*").intercept(LoggingInterceptor.class);
            interceptorSupport().forUri("/hello").interceptIn(AuthInterceptor.class);

            server().port(8888);
        }
    }
}
