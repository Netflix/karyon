package com.netflix.karyon.server.http;

import com.google.common.base.Preconditions;
import com.netflix.karyon.server.http.interceptor.PipelineFactory;
import com.netflix.karyon.server.http.spi.HttpRequestRouter;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.oio.OioServerSocketChannel;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


/**
 * An {@link HttpServer} using netty's {@link NioServerSocketChannel} i.e. non-blocking I/O. <br/>
 *
 * <h2>Blocking routers</h2>
 *
 * If the configured {@link HttpRequestRouter} is blocking as suggested by {@link HttpRequestRouter#isBlocking()} then
 * it is invoked in a different thread pool (netty's event executor). <br/>
 *
 * <h2>Non-blocking routers</h2>
 *
 * If the configured {@link HttpRequestRouter} is non-blocking as suggested by {@link HttpRequestRouter#isBlocking()} then
 * it is invoked in the event loop. <br/>
 *
 * <h2>Architecture</h2>
 *  See {@link com.netflix.karyon.server.http} for architecture details.
 *
 * {@link NonBlockingHttpServerBuilder} is an easy way to create an instance of {@link NonBlockingHttpServer}
 *
 * @see com.netflix.karyon.server.http
 *
 * @author Nitesh Kant
 */
public class NonBlockingHttpServer extends HttpServer {

    private final int routerExecutorThreads;
    @Nullable
    private final PipelineFactory interceptorFactory;

    public NonBlockingHttpServer(@Nonnull ServerBootstrap bootstrap,
                                 @Nonnull HttpRequestRouter httpRequestRouter,
                                 @Nullable PipelineFactory interceptorFactory,
                                 @Nullable com.netflix.karyon.server.ServerBootstrap karyonBootstrap) {
        this(bootstrap, httpRequestRouter, 0, interceptorFactory, karyonBootstrap);
        Preconditions.checkArgument(httpRequestRouter.isBlocking(),
                                    "The request router is blocking and no threads configured for router event executor.");
    }

    public NonBlockingHttpServer(@Nonnull ServerBootstrap bootstrap,
                                 @Nonnull HttpRequestRouter httpRequestRouter,
                                 int routerExecutorThreads,
                                 @Nullable PipelineFactory interceptorFactory,
                                 @Nullable com.netflix.karyon.server.ServerBootstrap karyonBootstrap) {
        super(bootstrap, httpRequestRouter, karyonBootstrap);
        this.interceptorFactory = interceptorFactory;
        if(httpRequestRouter.isBlocking() && routerExecutorThreads <= 0) {
            throw new IllegalArgumentException(
                    "The request router is blocking and non-zero/negative number of threads configured for router event executor.");
        }
        this.routerExecutorThreads = routerExecutorThreads;
    }

    @Override
    protected void addRouterToPipeline(SocketChannel ch) {
        if (httpRequestRouter.isBlocking()) {
            EventExecutorGroup eventExecutor = new DefaultEventExecutorGroup(routerExecutorThreads);
            ch.pipeline().addLast(eventExecutor, "router", new ServerHandler(httpRequestRouter, interceptorFactory));
        } else {
            ch.pipeline().addLast("router", new ServerHandler(httpRequestRouter, interceptorFactory));
        }
    }
}
