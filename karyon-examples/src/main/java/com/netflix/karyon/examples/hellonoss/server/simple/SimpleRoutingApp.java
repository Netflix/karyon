package com.netflix.karyon.examples.hellonoss.server.simple;

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
import com.netflix.karyon.examples.hellonoss.server.simple.SimpleRoutingApp.KaryonRxRouterModuleImpl;
import com.netflix.karyon.transport.http.KaryonHttpModule;
import com.netflix.karyon.transport.http.SimpleUriRouter;
import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import io.reactivex.netty.protocol.http.server.RequestHandler;
import io.reactivex.netty.servo.ServoEventsListenerFactory;
import rx.Observable;

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
public interface SimpleRoutingApp {

    class KaryonRxRouterModuleImpl extends KaryonHttpModule<ByteBuf, ByteBuf> {

        public KaryonRxRouterModuleImpl() {
            super("httpServerA", ByteBuf.class, ByteBuf.class);
        }

        @Override
        protected void configureServer() {
            final HelloWorldEndpoint endpoint = new HelloWorldEndpoint();
            SimpleUriRouter<ByteBuf, ByteBuf> router = new SimpleUriRouter<ByteBuf, ByteBuf>();

            router.addUri("/hello", new RequestHandler<ByteBuf, ByteBuf>() {
                @Override
                public Observable<Void> handle(HttpServerRequest<ByteBuf> request,
                                               HttpServerResponse<ByteBuf> response) {
                    return endpoint.sayHello(response);
                }
            }).addUri("/hello/to/*", new RequestHandler<ByteBuf, ByteBuf>() {
                @Override
                public Observable<Void> handle(HttpServerRequest<ByteBuf> request,
                                               HttpServerResponse<ByteBuf> response) {
                    return endpoint.sayHelloToUser(request, response);
                }
            });

            bindRouter().toInstance(router);
            bind(AuthenticationService.class).to(AuthenticationServiceImpl.class);
            interceptorSupport().forUri("/*").intercept(LoggingInterceptor.class);
            interceptorSupport().forUri("/hello").interceptIn(AuthInterceptor.class);

            bindEventsListenerFactory().to(ServoEventsListenerFactory.class);
            server().port(8888).threadPoolSize(100);
        }
    }
}
