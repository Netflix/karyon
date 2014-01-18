package com.netflix.karyon.server;

import com.netflix.karyon.server.spi.ChannelPipelineConfigurator;
import com.netflix.karyon.server.spi.RequestRouter;
import com.netflix.karyon.server.spi.ResponseWriterFactory;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A {@link KaryonNettyServer} using netty's {@link NioServerSocketChannel} i.e. non-blocking I/O. <br/>
 *
 * <h2>Blocking routers</h2>
 *
 * If the configured {@link RequestRouter} is blocking then it is invoked in a different thread pool
 * (netty's event executor). <br/>
 *
 * <h2>Non-blocking routers</h2>
 *
 * If the configured {@link RequestRouter} is non-blocking then it is invoked in the event loop. <br/>
 *
 * <h2>Architecture</h2>
 *  See {@link com.netflix.karyon.server} for architecture details.
 *
 * {@link NonBlockingKaryonServerBuilder} is an easy way to create an instance of {@link NonBlockingKaryonServer}
 *
 * @see com.netflix.karyon.server
 *
 * @author Nitesh Kant
 */
public class NonBlockingKaryonServer<I, O> extends KaryonNettyServer<I, O> {

    protected NonBlockingKaryonServer(@Nonnull io.netty.bootstrap.ServerBootstrap bootstrap,
                                      @Nonnull ChannelPipelineConfigurator<I, O> pipelineConfigurator,
                                      @Nonnull ResponseWriterFactory<O> responseWriterFactory,
                                      @Nullable ServerBootstrap karyonBootstrap) {
        super(bootstrap, pipelineConfigurator, responseWriterFactory, karyonBootstrap);
    }

    @Override
    protected boolean shouldRunBlockingRouterInAnExecutor() {
        return true;
    }
}
