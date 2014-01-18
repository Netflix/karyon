package com.netflix.karyon.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.handler.logging.LogLevel;

import javax.annotation.Nullable;

/**
 * A builder to build instances of {@link BlockingKaryonServer}.
 *
 * @param <I> The request type served by the server.
 * @param <O> The response type emitted by the server.
 */
@SuppressWarnings("rawtypes")
public class BlockingKaryonServerBuilder<I, O>
        extends KaryonNettyServerBuilder<BlockingKaryonServerBuilder, BlockingKaryonServer<I, O>, I, O>
        implements BlockingServerBuilderAttributes<BlockingKaryonServerBuilder<I, O>> {

    private final BlockingServerBuilderAttributesImpl<BlockingKaryonServerBuilder<I, O>> blockingServerBuilderAttributes;

    protected BlockingKaryonServerBuilder(int serverPort, @Nullable LogLevel nettyLoggerLevel) {
        super(serverPort, nettyLoggerLevel);
        blockingServerBuilderAttributes = new BlockingServerBuilderAttributesImpl<BlockingKaryonServerBuilder<I, O>>(this);
    }

    @Override
    public BlockingKaryonServerBuilder<I, O> withWorkerCount(int workerCount) {
        return blockingServerBuilderAttributes.withWorkerCount(workerCount);
    }

    @Override
    protected BlockingKaryonServer<I, O> createServer() {
        return new BlockingKaryonServer<I, O>(nettyBootstrap, pipelineConfigurator, responseWriterFactory,
                                              karyonBootstrap);
    }

    @Override
    protected void configureNettyBootstrap(ServerBootstrap nettyBootstrap) {
        blockingServerBuilderAttributes.configurOIOattributesInBootstrap(nettyBootstrap);
    }
}
