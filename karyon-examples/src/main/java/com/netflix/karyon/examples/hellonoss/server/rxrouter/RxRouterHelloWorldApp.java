package com.netflix.karyon.examples.hellonoss.server.rxrouter;

import com.google.inject.Singleton;
import com.netflix.adminresources.resources.KaryonWebAdminModule;
import com.netflix.governator.annotations.Modules;
import com.netflix.karyon.KaryonBootstrap;
import com.netflix.karyon.ShutdownModule;
import com.netflix.karyon.archaius.ArchaiusBootstrap;
import com.netflix.karyon.examples.hellonoss.server.LoggingInterceptor;
import com.netflix.karyon.examples.hellonoss.server.auth.AuthInterceptor;
import com.netflix.karyon.examples.hellonoss.server.auth.AuthenticationService;
import com.netflix.karyon.examples.hellonoss.server.auth.AuthenticationServiceImpl;
import com.netflix.karyon.examples.hellonoss.server.health.HealthCheck;
import com.netflix.karyon.examples.hellonoss.server.rxrouter.RxRouterHelloWorldApp.KaryonRxRouterModuleImpl;
import com.netflix.karyon.transport.http.KaryonHttpModule;
import io.netty.buffer.ByteBuf;
import io.reactivex.netty.servo.ServoEventsListenerFactory;

/**
 * @author Tomasz Bak
 */
@ArchaiusBootstrap
@KaryonBootstrap(name = "hello-netflix-oss", healthcheck = HealthCheck.class)
@Singleton
@Modules(include = {
        ShutdownModule.class,
        KaryonWebAdminModule.class,
        // KaryonEurekaModule.class, // Uncomment this to enable Eureka client.
        KaryonRxRouterModuleImpl.class
})
public interface RxRouterHelloWorldApp {

    class KaryonRxRouterModuleImpl extends KaryonHttpModule<ByteBuf, ByteBuf> {

        public KaryonRxRouterModuleImpl() {
            super("httpServerA", ByteBuf.class, ByteBuf.class);
        }

        @Override
        protected void configureServer() {
            bindRouter().to(HelloWorldRoute.class);
            bind(AuthenticationService.class).to(AuthenticationServiceImpl.class);
            interceptorSupport().forUri("/*").intercept(LoggingInterceptor.class);
            interceptorSupport().forUri("/hello").interceptIn(AuthInterceptor.class);

            bindEventsListenerFactory().to(ServoEventsListenerFactory.class);
            server().port(8888).threadPoolSize(100);
        }
    }
}
