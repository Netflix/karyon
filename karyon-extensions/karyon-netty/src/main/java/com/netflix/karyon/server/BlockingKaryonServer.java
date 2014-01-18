package com.netflix.karyon.server;

import com.netflix.karyon.server.spi.ChannelPipelineConfigurator;
import com.netflix.karyon.server.spi.ResponseWriterFactory;
import io.netty.channel.ChannelOption;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.channel.socket.oio.OioServerSocketChannel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A {@link KaryonNettyServer} using netty's {@link OioServerSocketChannel} i.e. blocking I/O. <br/>
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
 * {@link BlockingKaryonServerBuilder#serverSocketOption(ChannelOption, Object)} with a desired value.
 *
 * <h1>Architecture</h1>
 *  See {@link com.netflix.karyon.server} for architecture details.
 *
 * {@link BlockingKaryonServerBuilder} is an easy way to create an instance of {@link BlockingKaryonServer}
 *
 * @see com.netflix.karyon.server
 *
 * @author Nitesh Kant
 */
public class BlockingKaryonServer<I, O> extends KaryonNettyServer<I, O> {

    protected BlockingKaryonServer(@Nonnull io.netty.bootstrap.ServerBootstrap bootstrap,
                                   @Nonnull ChannelPipelineConfigurator<I, O> pipelineConfigurator,
                                   @Nonnull ResponseWriterFactory<O> responseWriterFactory,
                                   @Nullable ServerBootstrap karyonBootstrap) {
        super(bootstrap, pipelineConfigurator, responseWriterFactory, karyonBootstrap);
    }

    @Override
    protected boolean shouldRunBlockingRouterInAnExecutor() {
        return false;
    }
}
