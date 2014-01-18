package com.netflix.karyon.server.http;

import com.netflix.karyon.server.KaryonNettyServer;
import com.netflix.karyon.server.http.interceptor.PipelineFactory;
import com.netflix.karyon.server.spi.ChannelPipelineConfigurator;
import com.netflix.karyon.server.spi.ResponseWriterFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.handler.codec.http.HttpObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * An extension of {@link KaryonNettyServer} for HTTP protocol.
 *
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
public class BlockingHttpServer<I extends HttpObject, O extends HttpObject> extends HttpServer<I, O> {

    BlockingHttpServer(@Nonnull ServerBootstrap bootstrap,
                       @Nullable PipelineFactory<I, O> interceptorFactory,
                       @Nonnull ChannelPipelineConfigurator<I, O> pipelineConfigurator,
                       @Nonnull ResponseWriterFactory<O> responseWriterFactory,
                       @Nullable com.netflix.karyon.server.ServerBootstrap karyonBootstrap) {
        super(bootstrap, pipelineConfigurator, responseWriterFactory, interceptorFactory, karyonBootstrap);
    }

    @Override
    protected boolean shouldRunBlockingRouterInAnExecutor() {
        return false;
    }
}
