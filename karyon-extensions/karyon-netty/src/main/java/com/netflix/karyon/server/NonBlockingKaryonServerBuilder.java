package com.netflix.karyon.server;

import com.netflix.karyon.server.spi.BlockingRequestRouter;
import com.netflix.karyon.server.spi.ChannelPipelineConfigurator;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.handler.logging.LogLevel;

import javax.annotation.Nullable;

/**
 * A builder to build instances of {@link NonBlockingKaryonServer}.
 *
 * <h2>Event loops</h2>
 *
 * Every HttpServer requires two event loop groups, one for accepting new connections and another for serving a
 * particular client connection. This builder uses the same event loop group for both these event loops.<br/>
 * Since, this server only serves a single server port, there is always a single event loop required for accepting
 * new client connections. So, there is hardly ever a reason to specify more than one event loop for accepting new
 * connections & hence this builder does not allow configuring one.
 *
 * @see com.netflix.karyon.server
 *
 * @author Nitesh Kant
 */
@SuppressWarnings("rawtypes")
public class NonBlockingKaryonServerBuilder<I, O>
        extends KaryonNettyServerBuilder<NonBlockingKaryonServerBuilder, NonBlockingKaryonServer<I, O>, I, O>
        implements NonBlockingServerBuilderAttributes<NonBlockingKaryonServerBuilder<I, O>, I, O> {

    private final NonBlockingServerBuilderAttributesImpl<NonBlockingKaryonServerBuilder<I, O>, I, O> nonBlockingServerBuilderAttributes;

    protected NonBlockingKaryonServerBuilder(int serverPort, @Nullable LogLevel nettyLoggerLevel) {
        super(serverPort, nettyLoggerLevel);
        nonBlockingServerBuilderAttributes =
                new NonBlockingServerBuilderAttributesImpl<NonBlockingKaryonServerBuilder<I, O>, I, O>(this, pipelineConfigurator);
    }

    @Override
    public NonBlockingKaryonServerBuilder<I, O> withSelectorCount(int selectorCount) {
        return nonBlockingServerBuilderAttributes.withSelectorCount(selectorCount);
    }

    @Override
    public NonBlockingKaryonServerBuilder pipelineConfigurator(ChannelPipelineConfigurator<I, O> pipelineConfigurator) {
        nonBlockingServerBuilderAttributes.setPipelineConfigurator(pipelineConfigurator); // base class will set the state on this new configurator
        return super.pipelineConfigurator(pipelineConfigurator);
    }

    @Override
    public NonBlockingKaryonServerBuilder<I, O> requestRouter(BlockingRequestRouter<I, O> requestRouter,
                                                              int executorThreadCount) {
        return nonBlockingServerBuilderAttributes.requestRouter(requestRouter, executorThreadCount);
    }

    @Override
    protected NonBlockingKaryonServer<I, O> createServer() {
        return new NonBlockingKaryonServer<I, O>(nettyBootstrap, pipelineConfigurator, responseWriterFactory,
                                                 karyonBootstrap);
    }

    @Override
    protected void configureNettyBootstrap(ServerBootstrap nettyBootstrap) {
        nonBlockingServerBuilderAttributes.configurNIOattributesInBootstrap(nettyBootstrap);
    }

    @Override
    protected void validate() {
        super.validate();
        nonBlockingServerBuilderAttributes.validate();
    }
}
