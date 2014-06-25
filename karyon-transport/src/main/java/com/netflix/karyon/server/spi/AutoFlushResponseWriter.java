package com.netflix.karyon.server.spi;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.util.concurrent.Future;

/**
 * Default implementation of {@link ResponseWriter} which simply hands-off the write to netty.
 *
 * @author Nitesh Kant
 */
public class AutoFlushResponseWriter<T> implements ResponseWriter<T> {

    protected final ChannelHandlerContext context;

    public AutoFlushResponseWriter(ChannelHandlerContext context) {
        this.context = context;
    }

    @Override
    public Future<Void> write(T response) {
        return context.writeAndFlush(response);
    }

    public Future<Void> write(T response, ChannelPromise promise) {
        return context.writeAndFlush(response, promise);
    }

    @Override
    public ChannelHandlerContext getChannelHandlerContext() {
        return context;
    }
}
