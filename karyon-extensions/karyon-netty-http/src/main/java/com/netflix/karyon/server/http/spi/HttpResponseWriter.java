package com.netflix.karyon.server.http.spi;

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
 * and {@link #sendResponse()}. <br/>
 *
 * The implementations are <em>NOT</em> required to be thread-safe.
 * @author Nitesh Kant
 */
public interface HttpResponseWriter {

    /**
     * Creates a response for this writer with an optional content. <br/>
     *
     * @param responseStatus The HTTP response status of the response.
     * @param content Optional content buffer.
     *
     * @return The response object.
     *
     * @throws IllegalStateException If the response is already created.
     */
    FullHttpResponse createResponse(HttpResponseStatus responseStatus, @Nullable ByteBuf content);

    /**
     * Sends the response created by {@link #createResponse(HttpResponseStatus, ByteBuf)}. If the response is already
     * sent, this call is ignored.
     *
     * @throws IllegalStateException If no response was created before i.e. no calls to
     * {@link #createResponse(HttpResponseStatus, ByteBuf)} were made before this.
     */
    void sendResponse();

    /**
     * Returns the response instance, if created, as suggested by {@link #isResponseCreated()}
     *
     * @return The response instance, if created, else {@code null}
     */
    @Nullable
    FullHttpResponse response();

    /**
     * Asserts whether the response is created or not.
     *
     * @return {@code true} if the response is created, else, {@code false}.
     */
    boolean isResponseCreated();

    /**
     * Asserts whether the response is sent or not.
     *
     * @return {@code true} if the response is sent, else, {@code false}.
     */
    boolean isResponseSent();

    /**
     * Returns the underlying netty's {@link ChannelHandlerContext}.
     *
     * @return Netty's {@link ChannelHandlerContext}
     */
    ChannelHandlerContext getChannelHandlerContext();
}
