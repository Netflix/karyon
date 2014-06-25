package com.netflix.karyon.server.spi;

import io.netty.channel.ChannelHandlerContext;

/**
 * A factory for creating new instances of {@link ResponseWriter} per request.
 *
 * @author Nitesh Kant
 */
public interface ResponseWriterFactory<O> {

    /**
     * This is called exaclty once per request for creating a new instance of {@link ResponseWriter}
     *
     * @param channelHandlerContext Context associated with the request.
     *
     * @return The new instance of the writer.
     */
    ResponseWriter<O> newWriter(ChannelHandlerContext channelHandlerContext);
}
