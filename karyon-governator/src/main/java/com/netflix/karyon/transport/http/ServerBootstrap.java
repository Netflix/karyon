package com.netflix.karyon.transport.http;

import com.google.inject.Injector;
import com.netflix.governator.guice.lazy.FineGrainedLazySingleton;
import com.netflix.governator.lifecycle.LifecycleManager;
import com.netflix.karyon.ShutdownListener;
import io.reactivex.netty.protocol.http.server.HttpServer;
import io.reactivex.netty.protocol.http.server.HttpServerBuilder;
import io.reactivex.netty.protocol.http.server.RequestHandler;
import io.reactivex.netty.servo.ServoEventsListenerFactory;
import io.reactivex.netty.servo.http.HttpServerListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.functions.Action0;

import javax.inject.Inject;

/**
 * @author Nitesh Kant
 */
@FineGrainedLazySingleton
public class ServerBootstrap<I, O> {

    private static final Logger logger = LoggerFactory.getLogger(ServerBootstrap.class);

    private final int shutdownPort;
    private final HttpServerBuilder<I, O> serverBuilder;
    private ShutdownListener shutdownListener; // To avoid GC
    private HttpServer<I, O> server; // To avoid GC
    private final LifecycleManager lifecycleManager;

    @Inject
    public ServerBootstrap(Ports ports, Injector injector, RequestHandler<I, O> router,
                           LazyDelegateRouter<I, O> lazyDelegateRouter, HttpServerBuilder<I, O> serverBuilder) {
        shutdownPort = ports.getShutdownPort();
        this.serverBuilder = serverBuilder;
        lazyDelegateRouter.setRouter(injector, router);
        lifecycleManager = injector.getInstance(LifecycleManager.class);
    }

    public void startServer() throws Exception {
        _start();
        server.startAndWait();
    }

    public void startServerAndWait() throws Exception {
        startServer();
        server.waitTillShutdown();
    }

    protected void _start() throws Exception {
        server = serverBuilder.build();
        ServoEventsListenerFactory factory = new ServoEventsListenerFactory();
        HttpServerListener listener = factory.forHttpServer(server);
        server.subscribe(listener);

        shutdownListener = new ShutdownListener(shutdownPort, new Action0() {
            @Override
            public void call() {
                try {
                    server.shutdown();
                } catch (InterruptedException e) {
                    logger.error("Failed to shutdown server.", e);
                }
                lifecycleManager.close();
            }
        });
        shutdownListener.start();
        lifecycleManager.start();
    }
}
