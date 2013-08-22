package com.netflix.karyon.server.http;

import com.netflix.karyon.server.http.filter.PipelineFactory;
import com.netflix.karyon.server.http.spi.HttpRequestRouter;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.socket.SocketChannel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author Nitesh Kant
 */
public class BlockingHttpServer extends HttpServer {

    @Nullable
    private final PipelineFactory filterFactory;

    BlockingHttpServer(@Nonnull ServerBootstrap bootstrap,
                       @Nonnull HttpRequestRouter httpRequestRouter,
                       @Nullable PipelineFactory filterFactory,
                       @Nullable com.netflix.karyon.server.ServerBootstrap karyonBootstrap) {
        super(bootstrap, httpRequestRouter, karyonBootstrap);
        this.filterFactory = filterFactory;
    }

    @Override
    protected void addRouterToPipeline(SocketChannel ch) {
        ch.pipeline().addLast("router", new ServerHandler(httpRequestRouter, filterFactory));
    }
}
