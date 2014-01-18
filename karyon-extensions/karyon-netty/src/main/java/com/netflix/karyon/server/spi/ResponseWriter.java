package com.netflix.karyon.server.spi;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Future;

/**
 * An abstraction for writing responses (one or many) back to the associated channel. <br/>
 * There is always a single instance of {@link ResponseWriter} associated with a channel, irrepective of whether the
 * protocol supports a single request-response communication (plain old HTTP), server push (SSE) or a full-duplex
 * communication. (websockets) <br/>
 * A response writer is always thread-safe, so multiple threads can write responses to the writer, if supported by the
 * protocol. Netty internally though, will queue these responses as one channel is always handled by a single event
 * loop.
 *
 * @author Nitesh Kant
 */
public interface ResponseWriter<T> {

    /**
     * Writes a response to the associated channel. This method can be invoked multiple times on a writer if it is
     * supported by the underlying protocol.
     *
     * @param response The response to write.
     *
     * @return The future associated with completion of this write operation on the channel.
     */
    Future<Void> write(T response);

    /**
     * Returns the underlying netty's {@link ChannelHandlerContext}.
     *
     * @return Netty's {@link ChannelHandlerContext}
     */
    ChannelHandlerContext getChannelHandlerContext();
}
