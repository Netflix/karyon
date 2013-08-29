package com.netflix.karyon.server.http.interceptor;

import com.netflix.karyon.server.http.spi.HttpResponseWriter;
import io.netty.handler.codec.http.FullHttpRequest;

/**
 * Interceptors are based on the <a href="http://en.wikipedia.org/wiki/Interceptor_pattern">Interceptor pattern</a> and
 * are used to implement cross-cutting concerns across all or a set of HTTP requests. <br/>
 *
 * @see com.netflix.karyon.server.http.interceptor
 */
public interface Interceptor {

    /**
     * Executes this interceptor.
     *
     * @param httpRequest Request for which this interceptor is invoked.
     * @param responseWriter Response writer.
     * @param executionContext Execution context for this execution.
     */
    void filter(FullHttpRequest httpRequest, HttpResponseWriter responseWriter, InterceptorExecutionContext executionContext);
}
