package com.netflix.karyon.server.http.interceptor;

import com.netflix.karyon.server.http.spi.HttpResponseWriter;
import io.netty.handler.codec.http.FullHttpRequest;

/**
 * Interceptor that is called by karyon when the router has finished processing and has invoked
 * {@link HttpResponseWriter#sendResponse()} to indicate writing of response back to the client.
 *
 * @see com.netflix.karyon.server.http.interceptor
 *
 * @author Nitesh Kant
 */
public interface OutboundInterceptor {

    /**
     * Executes this interceptor.
     *
     * @param httpRequest Request for which this interceptor is invoked.
     * @param responseWriter Response writer.
     * @param executionContext Execution context for this execution.
     */
    void interceptOut(FullHttpRequest httpRequest, HttpResponseWriter responseWriter,
                      InterceptorExecutionContext executionContext);
}
