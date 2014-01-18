package com.netflix.karyon.server;

import com.google.common.base.Preconditions;
import com.netflix.karyon.server.spi.BlockingRequestRouter;
import com.netflix.karyon.server.spi.ChannelPipelineConfigurator;
import com.netflix.karyon.server.spi.DefaultChannelPipelineConfigurator;
import com.netflix.karyon.server.spi.DefaultResponseWriterFactory;
import com.netflix.karyon.server.spi.RequestRouter;
import com.netflix.karyon.server.spi.ResponseWriterFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.handler.logging.LogLevel;

import javax.annotation.Nullable;

/**
 * A builder to ease creation of {@link KaryonNettyServer}. Unless you are providing a new builder you should be looking
 * at {@link BlockingKaryonServerBuilder} or {@link NonBlockingKaryonServerBuilder}
 *
 * @author Nitesh Kant
 */
@SuppressWarnings("rawtypes")
public abstract class KaryonNettyServerBuilder<B extends KaryonNettyServerBuilder, S extends KaryonNettyServer<I, O>, I, O> {

    protected final ServerBootstrap nettyBootstrap;
    private final String uniqueServerName;
    protected com.netflix.karyon.server.ServerBootstrap karyonBootstrap;
    protected ChannelPipelineConfigurator<I, O> pipelineConfigurator;
    protected ResponseWriterFactory<O> responseWriterFactory;

    protected KaryonNettyServerBuilder(int serverPort, @Nullable LogLevel nettyLoggerLevel) {
        uniqueServerName = createUniqueServerName(serverPort);
        nettyBootstrap = new ServerBootstrap();
        nettyBootstrap.localAddress(serverPort);
        pipelineConfigurator = new DefaultChannelPipelineConfigurator<I, O>(uniqueServerName,
                                                                            null == nettyLoggerLevel
                                                                            ? LogLevel.DEBUG : nettyLoggerLevel);
        responseWriterFactory = new DefaultResponseWriterFactory<O>();
    }

    protected static String createUniqueServerName(int serverPort) {
        return "KaryonNettyServer-" + serverPort;
    }

    public <O> B serverSocketOption(ChannelOption<O> channelOption, O value) {
        nettyBootstrap.option(channelOption, value);
        return returnBuilder();
    }

    public <O> B clientSocketOption(ChannelOption<O> channelOption, O value) {
        nettyBootstrap.childOption(channelOption, value);
        return returnBuilder();
    }

    public B requestRouter(RequestRouter<I, O> requestRouter) {
        pipelineConfigurator.setRouter(requestRouter);
        return returnBuilder();
    }

    public B karyonBootstrap(com.netflix.karyon.server.ServerBootstrap karyonBootstrap) {
        this.karyonBootstrap = karyonBootstrap;
        return returnBuilder();
    }

    public B pipelineConfigurator(ChannelPipelineConfigurator<I, O> pipelineConfigurator) {
        ChannelPipelineConfigurator<I, O> oldPipelineConfigurator = this.pipelineConfigurator;
        this.pipelineConfigurator = pipelineConfigurator;
        RequestRouter<I, O> router = oldPipelineConfigurator.getRouter();
        if (null != router) {
            if (RequestRouter.RoutersNatureIdentifier.isBlocking(router)) {
                BlockingRequestRouter<I, O> blockingRouter = (BlockingRequestRouter<I, O>) router;
                this.pipelineConfigurator.setRouter(blockingRouter, oldPipelineConfigurator.getRouterExecutorGroup());
            } else {
                this.pipelineConfigurator.setRouter(router);
            }
        }
        return returnBuilder();
    }

    public B responseWriterFactory(ResponseWriterFactory<O> responseWriterFactory) {
        this.responseWriterFactory = responseWriterFactory;
        return returnBuilder();
    }

    public S build() {
        validate();
        configureNettyBootstrap(nettyBootstrap);
        return createServer();
    }

    public String getUniqueServerName() {
        return uniqueServerName;
    }

    /**
     * Creates the actual instance of the {@link KaryonNettyServer}
     *
     * @return The instance of server.
     */
    protected abstract S createServer();

    /**
     * Configures the {@link ServerBootstrap} instance used by this builder, typically to configure the
     * {@link EventLoopGroup} and the {@link ServerSocketChannel}
     *
     * @param nettyBootstrap Netty bootstrap to configure.
     */
    protected abstract void configureNettyBootstrap(ServerBootstrap nettyBootstrap);

    @SuppressWarnings("unchecked")
    protected B returnBuilder() {
        return (B) this;
    }

    protected void validate() {
        Preconditions.checkState(null != pipelineConfigurator.getRouter(), "Request router is not set.");
    }
}
