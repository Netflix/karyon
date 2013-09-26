package com.netflix.karyon.server.http;

import com.netflix.karyon.server.http.interceptor.PipelineFactory;
import com.netflix.karyon.server.http.spi.HttpRequestRouter;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.oio.OioServerSocketChannel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * An {@link HttpServer} using netty's {@link OioServerSocketChannel} i.e. blocking I/O. <br/>
 * Irrespective of the fact whether the configured router is blocking or non-blocking, the router is invoked in netty's
 * event loop as there is one thread per client connection. <br/>
 *
 * <h2>Excessive clients</h2>
 * The server stops accepting new connections when all the event threads are busy serving clients i.e. the number of
 * concurrent clients are equal to the size of the configured {@link OioEventLoopGroup}. Strictly speaking, the number
 * of clients is the size of event loop group - 1 as one event thread is used for accepting new client connections. So,
 * it is mandatory to configure the event loop group size according to the number of concurrent clients one wishes to
 * serve. <br/>
 * The backlog of requested client connections which are not yet accepted by the server can be configured setting an
 * option {@link ChannelOption#SO_BACKLOG} on the server socket. This can be done by invoking
 * {@link BlockingHttpServerBuilder#serverSocketOption(ChannelOption, Object)} with a desired value.
 *
 * <h1>Architecture</h1>
 *  See {@link com.netflix.karyon.server.http} for architecture details.
 *
 * {@link BlockingHttpServerBuilder} is an easy way to create an instance of {@link BlockingHttpServer}
 *
 * @see com.netflix.karyon.server.http
 *
 * @author Nitesh Kant
 */
public class BlockingHttpServer extends HttpServer {

    @Nullable
    private final PipelineFactory interceptorFactory;

    BlockingHttpServer(@Nonnull ServerBootstrap bootstrap,
                       @Nonnull HttpRequestRouter httpRequestRouter,
                       @Nullable PipelineFactory interceptorFactory,
                       @Nullable com.netflix.karyon.server.ServerBootstrap karyonBootstrap) {
        super(bootstrap, httpRequestRouter, karyonBootstrap);
        this.interceptorFactory = interceptorFactory;
    }

    @Override
    protected void addRouterToPipeline(SocketChannel ch) {
        ch.pipeline().addLast("router", new ServerHandler(httpRequestRouter, interceptorFactory));
    }
}
