package com.netflix.hellonoss.server;

import com.netflix.adminresources.resources.KaryonWebAdminModule;
import com.netflix.governator.annotations.Modules;
import com.netflix.hellonoss.server.auth.AuthInterceptor;
import com.netflix.hellonoss.server.auth.AuthenticationService;
import com.netflix.hellonoss.server.auth.AuthenticationServiceImpl;
import com.netflix.hellonoss.server.health.HealthCheck;
import com.netflix.karyon.KaryonBootstrap;
import com.netflix.karyon.archaius.ArchaiusBootstrap;
import com.netflix.karyon.eureka.KaryonEurekaModule;
import com.netflix.karyon.jersey.blocking.KaryonJerseyModule;
import com.netflix.karyon.transport.http.GovernatorHttpInterceptorSupport;
import io.netty.buffer.ByteBuf;

@ArchaiusBootstrap
@KaryonBootstrap(name = "hello-netflix-oss", healthcheck = HealthCheck.class)
@Modules(include = {HelloWorldApp.KaryonJerseyModuleImpl.class, KaryonWebAdminModule.class
                    // Uncomment the following line to enable eureka. Make sure eureka-client.properties is configured to point to your eureka server.
                    //, KaryonEurekaModule.class
        })
public final class HelloWorldApp {

    public static class KaryonJerseyModuleImpl extends KaryonJerseyModule {

        @Override
        protected void configure() {
            super.configure();
            bind(AuthenticationService.class).to(AuthenticationServiceImpl.class);
        }

        @Override
        public int serverPort() {
            return 8888;
        }

        @Override
        public int shutdownPort() {
            return 8899;
        }

        @Override
        protected int requestProcessingThreadsCount() {
            return 100;
        }

        @Override
        public void configureInterceptors(GovernatorHttpInterceptorSupport<ByteBuf, ByteBuf> interceptorSupport) {
            interceptorSupport.forUri("/*").intercept(LoggingInterceptor.class);
            interceptorSupport.forUri("/hello").interceptIn(AuthInterceptor.class);
        }
    }
}
