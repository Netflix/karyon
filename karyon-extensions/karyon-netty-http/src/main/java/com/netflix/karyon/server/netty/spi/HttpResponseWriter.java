package com.netflix.karyon.server.netty.spi;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;

import javax.annotation.Nullable;

/**
 * This contract intends to hide the nuances of netty and provide a simpler interface that deals with response creation
 * and writing. At the same time it also provides the handle to netty's {@link ChannelHandlerContext} for advanced
 * users. <br/>
 *
 * The implementation must hold state between the invocations of {@link #createResponse(HttpResponseStatus, ByteBuf)}
 * and {@link #sendResponse()}.
 *
 * @author Nitesh Kant
 */
public interface HttpResponseWriter {

    /**
     * Creates a response for this writer with an optional content. <br/>
     *
     *
     * @param responseStatus The HTTP response status of the response.
     * @param content Optional content buffer.
     *
     * @return The response object.
     */
    FullHttpResponse createResponse(HttpResponseStatus responseStatus, @Nullable ByteBuf content);

    /**
     * Sends the response created by {@link #createResponse(HttpResponseStatus, ByteBuf)}.
     *
     * @throws IllegalStateException If no response was created before i.e. no calls to
     * {@link #createResponse(HttpResponseStatus, ByteBuf)} were made before this.
     */
    void sendResponse();

    /**
     * Returns the underlying netty's {@link ChannelHandlerContext}.
     *
     * @return Netty's {@link ChannelHandlerContext}
     */
    ChannelHandlerContext getChannelHandlerContext();
}
