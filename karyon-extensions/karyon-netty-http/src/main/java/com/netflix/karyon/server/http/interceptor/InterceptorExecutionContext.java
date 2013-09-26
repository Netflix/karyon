package com.netflix.karyon.server.http.interceptor;

import com.netflix.karyon.server.http.spi.HttpRequestRouter;
import com.netflix.karyon.server.http.spi.HttpResponseWriter;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

/**
 * A context for the processing of an entire interceptor chain. An instance of this context is created per request and
 * shared between inbound and outbound interceptors for the same request. <br/>
 *
 * <h2>State sharing</h2>
 * Implementations are encouraged to use netty's state sharing mechanism as defined in {@link ChannelHandler} using
 * {@link ChannelHandlerContext} which can be obtained using {@link HttpResponseWriter#getChannelHandlerContext()}
 *
 * @see com.netflix.karyon.server.http.interceptor
 *
 * @author Nitesh Kant
 */
public interface InterceptorExecutionContext {

    /**
     * Executes the next interceptor if present, otherwise proceeds to invoke:
     * <ul>
     <li>{@link HttpRequestRouter} for inbound interception.</li>
     <li>{@link HttpResponseWriter#sendResponse()} for outbound interception.</li>
     </ul>
     *
     * @param request Request
     * @param responseWriter Response writer.
     *
     * @see com.netflix.karyon.server.http.interceptor
     */
    void executeNextInterceptor(FullHttpRequest request, HttpResponseWriter responseWriter);
}
