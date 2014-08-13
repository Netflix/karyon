package com.netflix.karyon.examples.hellonoss.server;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.netflix.adminresources.resources.KaryonWebAdminModule;
import com.netflix.governator.annotations.Modules;
import com.netflix.karyon.KaryonBootstrap;
import com.netflix.karyon.Submodules;
import com.netflix.karyon.archaius.ArchaiusBootstrap;
import com.netflix.karyon.examples.hellonoss.server.auth.AuthInterceptor;
import com.netflix.karyon.examples.hellonoss.server.auth.AuthenticationService;
import com.netflix.karyon.examples.hellonoss.server.auth.AuthenticationServiceImpl;
import com.netflix.karyon.examples.hellonoss.server.health.HealthCheck;
import com.netflix.karyon.examples.rx.tcp.TcpPipelineServers.ApplicationModule;
import com.netflix.karyon.jersey.blocking.KaryonJerseyModule;
import com.netflix.karyon.transport.ServerPort;
import com.netflix.karyon.transport.http.GovernatorHttpInterceptorSupport;
import io.netty.buffer.ByteBuf;

@ArchaiusBootstrap
@KaryonBootstrap(name = "hello-netflix-oss", healthcheck = HealthCheck.class)
@Modules(include = ApplicationModule.class)
@Submodules(include = {HelloWorldApp.KaryonJerseyModuleImpl.class, KaryonWebAdminModule.class})
public interface HelloWorldApp {

    class KaryonJerseyModuleImpl extends KaryonJerseyModule {

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
        protected int requestProcessingThreadsCount() {
            return 100;
        }

        @Override
        public void configureInterceptors(GovernatorHttpInterceptorSupport<ByteBuf, ByteBuf> interceptorSupport) {
            interceptorSupport.forUri("/*").intercept(LoggingInterceptor.class);
            interceptorSupport.forUri("/hello").interceptIn(AuthInterceptor.class);
        }
    }

    class ApplicationModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(ServerPort.class).annotatedWith(Names.named("shutdown")).toInstance(new ServerPort(5555));
        }
    }
}
