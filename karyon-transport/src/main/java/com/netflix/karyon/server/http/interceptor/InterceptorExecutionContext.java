package com.netflix.karyon.server.http.interceptor;

import com.netflix.karyon.server.spi.ResponseWriter;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;

/**
 * A context for the processing of an entire interceptor chain. <br/>
 *
 * <h2>State sharing</h2>
 * Implementations are encouraged to use netty's state sharing mechanism as defined in {@link ChannelHandler} using
 * {@link ChannelHandlerContext} which can be obtained using {@link ResponseWriter#getChannelHandlerContext()}
 *
 * @param <I> Request object type correcponding to the pipeline.
 * @param <O> Response object type correcponding to the pipeline.
 *
 * @see com.netflix.karyon.server.http.interceptor
 * @author Nitesh Kant
 */
public interface InterceptorExecutionContext<I, O> {

    /**
     * Executes the next interceptor if present, otherwise sends the message (request/response) further in the netty
     * pipeline.
     *
     * @param message Request for {@link InboundInterceptor} and response for {@link OutboundInterceptor}
     * @param responseWriter Response writer.
     *
     * @see com.netflix.karyon.server.http.interceptor
     */
    void executeNextInterceptor(I message, ResponseWriter<O> responseWriter);
}
