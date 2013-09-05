package com.netflix.karyon.server.http.interceptor;

import com.netflix.karyon.server.http.spi.HttpRequestRouter;
import com.netflix.karyon.server.http.spi.HttpResponseWriter;
import io.netty.handler.codec.http.FullHttpRequest;

/**
 * Interceptor that is called by karyon before invoking {@link HttpRequestRouter}
 *
 * @see com.netflix.karyon.server.http.interceptor
 *
 * @author Nitesh Kant
 */
public interface InboundInterceptor {

    /**
     * Executes this interceptor.
     *
     * @param httpRequest Request for which this interceptor is invoked.
     * @param responseWriter Response writer.
     * @param executionContext Execution context for this execution.
     */
    void interceptIn(FullHttpRequest httpRequest, HttpResponseWriter responseWriter,
                     InterceptorExecutionContext executionContext);
}
