package com.netflix.karyon.server.http.interceptor;

import com.netflix.karyon.server.http.spi.HttpRequestRouter;
import com.netflix.karyon.server.http.spi.HttpResponseWriter;
import io.netty.handler.codec.http.FullHttpRequest;

/**
 * A context for the processing of an entire interceptor chain. An instance of this context is created per request.
 *
 * @author Nitesh Kant
 */
public interface InterceptorExecutionContext {

    /**
     * Executes the next interceptor if present, otherwise proceeds to invoke the {@link HttpRequestRouter}.
     *
     * @param request Request
     * @param responseWriter Response writer.
     *
     * @see com.netflix.karyon.server.http.interceptor
     */
    void executeNextInterceptor(FullHttpRequest request, HttpResponseWriter responseWriter);
}
