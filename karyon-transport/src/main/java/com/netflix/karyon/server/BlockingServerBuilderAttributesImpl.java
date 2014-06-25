package com.netflix.karyon.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.channel.socket.oio.OioServerSocketChannel;

/**
 * An implementation of {@link BlockingServerBuilderAttributes}
 *
 * @param <B> Any builder that uses this implementation.
 */
public class BlockingServerBuilderAttributesImpl<B> implements BlockingServerBuilderAttributes<B> {

    private final B enclosingBuilder;
    private int workerCount = DEFAULT_WORKER_COUNT;

    public BlockingServerBuilderAttributesImpl(B enclosingBuilder) {
        this.enclosingBuilder = enclosingBuilder;
    }

    @Override
    public B withWorkerCount(int workerCount) {
        this.workerCount = workerCount;
        return enclosingBuilder;
    }

    public int getWorkerCount() {
        return workerCount;
    }

    public void configurOIOattributesInBootstrap(ServerBootstrap nettyBootstrap) {
        nettyBootstrap.group(new OioEventLoopGroup(workerCount))
                      .channel(OioServerSocketChannel.class);
    }
}
