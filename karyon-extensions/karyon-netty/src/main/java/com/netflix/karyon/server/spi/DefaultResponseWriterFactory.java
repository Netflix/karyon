package com.netflix.karyon.server.spi;

import io.netty.channel.ChannelHandlerContext;

/**
 * Default implementation of {@link ResponseWriterFactory} that creates instances of {@link AutoFlushResponseWriter}
 *
 * @author Nitesh Kant
 */
public class DefaultResponseWriterFactory<O> implements ResponseWriterFactory<O> {

    @Override
    public AutoFlushResponseWriter<O> newWriter(ChannelHandlerContext channelHandlerContext) {
        return new AutoFlushResponseWriter<O>(channelHandlerContext);
    }
}
