package com.netflix.karyon.transport.http;

import javax.inject.Inject;

import com.google.inject.Injector;
import com.netflix.governator.guice.lazy.FineGrainedLazySingleton;
import com.netflix.karyon.transport.KaryonServerBootstrap;
import io.reactivex.netty.protocol.http.server.HttpServer;
import io.reactivex.netty.protocol.http.server.HttpServerBuilder;
import io.reactivex.netty.servo.ServoEventsListenerFactory;
import io.reactivex.netty.servo.http.HttpServerListener;

/**
 * @author Nitesh Kant
 */
@FineGrainedLazySingleton
public class HttpServerBootstrap<I, O> implements KaryonServerBootstrap {

    private final HttpServerBuilder<I, O> serverBuilder;
    private HttpServer<I, O> server; // To avoid GC

    @Inject
    public HttpServerBootstrap(Injector injector, HttpRequestRouter<I, O> router,
                               LazyDelegateRouter<I, O> lazyDelegateRouter, HttpServerBuilder<I, O> serverBuilder) {
        this.serverBuilder = serverBuilder;
        lazyDelegateRouter.setRouter(injector, router);
    }

    @Override
    public void startServer() throws Exception {
        _start();
        server.start();
    }

    @Override
    public void shutdown() throws InterruptedException {
        server.shutdown();
    }

    @Override
    public void waitTillShutdown() throws InterruptedException {
        server.waitTillShutdown();
    }

    protected void _start() throws Exception {
        server = serverBuilder.build();
        ServoEventsListenerFactory factory = new ServoEventsListenerFactory();
        HttpServerListener listener = factory.forHttpServer(server);
        server.subscribe(listener);
    }
}
