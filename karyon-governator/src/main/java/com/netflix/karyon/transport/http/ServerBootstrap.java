package com.netflix.karyon.transport.http;

import com.google.inject.Injector;
import com.netflix.governator.guice.lazy.FineGrainedLazySingleton;
import io.reactivex.netty.protocol.http.server.HttpServerBuilder;

import javax.inject.Inject;

/**
 * @author Nitesh Kant
 */
@FineGrainedLazySingleton
public class ServerBootstrap<I, O> {

    private final HttpServerBuilder<I, O> serverBuilder;

    @Inject
    public ServerBootstrap(Injector injector, HttpRequestRouter<I, O> router,
                           LazyDelegateRouter<I, O> lazyDelegateRouter, HttpServerBuilder<I, O> serverBuilder) {
        this.serverBuilder = serverBuilder;
        lazyDelegateRouter.setRouter(injector, router);
    }

    public void startServer() {
        serverBuilder.build().start();
    }

    public void startServerAndWait() {
        serverBuilder.build().startAndWait();
    }
}
