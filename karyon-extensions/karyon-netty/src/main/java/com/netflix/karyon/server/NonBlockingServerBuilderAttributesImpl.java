package com.netflix.karyon.server;

import com.netflix.karyon.server.spi.BlockingRequestRouter;
import com.netflix.karyon.server.spi.ChannelPipelineConfigurator;
import com.netflix.karyon.server.spi.RequestRouter;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultEventExecutorGroup;

/**
 * @author Nitesh Kant
 */
public class NonBlockingServerBuilderAttributesImpl<B, I, O> implements NonBlockingServerBuilderAttributes<B, I, O> {

    private final B enclosingBuilder;
    private ChannelPipelineConfigurator<I, O> pipelineConfigurator;
    private int selectorCount;

    public NonBlockingServerBuilderAttributesImpl(B enclosingBuilder, ChannelPipelineConfigurator<I, O> pipelineConfigurator) {
        this.enclosingBuilder = enclosingBuilder;
        this.pipelineConfigurator = pipelineConfigurator;
    }

    @Override
    public B withSelectorCount(int selectorCount) {
        this.selectorCount = selectorCount;
        return enclosingBuilder;
    }

    @Override
    public B requestRouter(BlockingRequestRouter<I, O> requestRouter, int executorThreadCount) {
        pipelineConfigurator.setRouter(requestRouter, new DefaultEventExecutorGroup(executorThreadCount));
        return enclosingBuilder;
    }

    public void configurNIOattributesInBootstrap(ServerBootstrap nettyBootstrap) {
        nettyBootstrap.group(new NioEventLoopGroup(selectorCount))
                      .channel(NioServerSocketChannel.class);
    }

    public void setPipelineConfigurator(ChannelPipelineConfigurator<I, O> pipelineConfigurator) {
        this.pipelineConfigurator = pipelineConfigurator;
    }

    public void validate() {
        if(RequestRouter.RoutersNatureIdentifier.isBlocking(pipelineConfigurator.getRouter())
           && pipelineConfigurator.getRouterExecutorGroup() == null) {
            throw new IllegalArgumentException("Blocking request router must specify executor thread count.");
        }
    }
}
