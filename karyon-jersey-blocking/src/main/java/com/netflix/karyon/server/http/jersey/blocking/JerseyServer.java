package com.netflix.karyon.server.http.jersey.blocking;

import com.google.inject.Injector;
import com.netflix.karyon.transport.KaryonTransport;
import com.netflix.karyon.transport.http.HttpInterceptorSupport;
import com.netflix.karyon.transport.http.HttpRequestHandler;
import com.netflix.karyon.transport.http.HttpRequestHandlerBuilder;
import io.netty.buffer.ByteBuf;
import io.reactivex.netty.pipeline.PipelineConfigurators;
import io.reactivex.netty.protocol.http.server.HttpServer;
import io.reactivex.netty.protocol.http.server.HttpServerBuilder;

/**
 * @author Nitesh Kant
 */
public class JerseyServer {

    private final HttpServer<ByteBuf, ByteBuf> rxServer;
    private JerseyBasedRouter router;

    protected JerseyServer(HttpServer<ByteBuf, ByteBuf> rxServer, JerseyBasedRouter router) {
        this.rxServer = rxServer;
        this.router = router;
    }

    public JerseyServer start() {
        router.start();
        rxServer.start();
        return this;
    }
    public void startAndWait() {
        router.start();
        rxServer.startAndWait();
    }

    public void shutdown() throws InterruptedException {
        router.stop();
        rxServer.shutdown();
    }

    public void waitTillShutdown() throws InterruptedException {
        rxServer.waitTillShutdown();
    }

    public static JerseyServer fromDefaults(int port) {
        JerseyBasedRouter router = JerseyRouterProvider.createRouter();
        return newServerBuilder(port, router);
    }

    public static JerseyServer fromDefaults(int port, Injector guiceInjector) {
        JerseyBasedRouter router = JerseyRouterProvider.createRouter(guiceInjector);
        return newServerBuilder(port, router);
    }

    public static JerseyServer from(int port, JerseyBasedRouter router) {
        return newServerBuilder(port, router);
    }

    public static JerseyServer from(int port, HttpInterceptorSupport<ByteBuf, ByteBuf> interceptorSupport) {
        JerseyBasedRouter router = JerseyRouterProvider.createRouter();
        return newServerBuilder(port, new HttpRequestHandlerBuilder<ByteBuf, ByteBuf>(interceptorSupport, router));
    }

    public static JerseyServer from(int port, HttpInterceptorSupport<ByteBuf, ByteBuf> interceptorSupport,
                                    Injector guiceInjector) {
        JerseyBasedRouter router = JerseyRouterProvider.createRouter(guiceInjector);
        return newServerBuilder(port, new HttpRequestHandlerBuilder<ByteBuf, ByteBuf>(interceptorSupport, router));
    }

    public static JerseyServer from(int port, JerseyBasedRouter router,
                                    HttpInterceptorSupport<ByteBuf, ByteBuf> interceptorSupport) {
        return newServerBuilder(port, new HttpRequestHandlerBuilder<ByteBuf, ByteBuf>(interceptorSupport, router));
    }

    protected static JerseyServer newServerBuilder(int port, HttpRequestHandlerBuilder<ByteBuf, ByteBuf> requestHandlerBuilder) {
        HttpRequestHandler<ByteBuf, ByteBuf> requestHandler = requestHandlerBuilder.build();
        HttpServerBuilder<ByteBuf, ByteBuf> builder = KaryonTransport.newHttpServerBuilder(port, requestHandler);
        HttpServer<ByteBuf, ByteBuf> server =
                builder.pipelineConfigurator(PipelineConfigurators.<ByteBuf, ByteBuf>httpServerConfigurator()).build();
        return new JerseyServer(server, (JerseyBasedRouter) requestHandlerBuilder.getRouter());
    }

    protected static JerseyServer newServerBuilder(int port, JerseyBasedRouter router) {
        HttpServerBuilder<ByteBuf, ByteBuf> builder = KaryonTransport.newHttpServerBuilder(port, router);
        HttpServer<ByteBuf, ByteBuf> server =
                builder.pipelineConfigurator(PipelineConfigurators.<ByteBuf, ByteBuf>httpServerConfigurator()).build();
        return new JerseyServer(server, router);
    }
}
