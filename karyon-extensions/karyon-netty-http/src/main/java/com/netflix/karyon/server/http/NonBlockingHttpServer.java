package com.netflix.karyon.server.http;

import com.google.common.base.Preconditions;
import com.netflix.karyon.server.http.filter.PipelineFactory;
import com.netflix.karyon.server.http.spi.HttpRequestRouter;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author Nitesh Kant
 */
public class NonBlockingHttpServer extends HttpServer {

    private final int routerExecutorThreads;
    @Nullable
    private final PipelineFactory filterFactory;

    public NonBlockingHttpServer(@Nonnull ServerBootstrap bootstrap,
                                 @Nonnull HttpRequestRouter httpRequestRouter,
                                 @Nullable PipelineFactory filterFactory,
                                 @Nullable com.netflix.karyon.server.ServerBootstrap karyonBootstrap) {
        this(bootstrap, httpRequestRouter, 0, filterFactory, karyonBootstrap);
        Preconditions.checkArgument(httpRequestRouter.isBlocking(),
                                    "The request router is blocking and no threads configured for router event executor.");
    }

    public NonBlockingHttpServer(@Nonnull ServerBootstrap bootstrap,
                                 @Nonnull HttpRequestRouter httpRequestRouter,
                                 int routerExecutorThreads,
                                 @Nullable PipelineFactory filterFactory,
                                 @Nullable com.netflix.karyon.server.ServerBootstrap karyonBootstrap) {
        super(bootstrap, httpRequestRouter, karyonBootstrap);
        this.filterFactory = filterFactory;
        Preconditions.checkArgument(httpRequestRouter.isBlocking() && routerExecutorThreads > 0,
                                    "The request router is blocking and non-zero/negative number of threads configured for router event executor.");
        this.routerExecutorThreads = routerExecutorThreads;
    }

    @Override
    protected void addRouterToPipeline(SocketChannel ch) {
        if (httpRequestRouter.isBlocking()) {
            EventExecutorGroup eventExecutor = new DefaultEventExecutorGroup(routerExecutorThreads);
            ch.pipeline().addLast(eventExecutor, "router", new ServerHandler(httpRequestRouter, filterFactory));
        }
    }
}
