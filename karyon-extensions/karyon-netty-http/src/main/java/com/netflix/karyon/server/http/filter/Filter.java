package com.netflix.karyon.server.http.filter;

import com.netflix.karyon.server.http.spi.HttpResponseWriter;
import io.netty.handler.codec.http.FullHttpRequest;

/**
 *
 */
public interface Filter {

    /**
     * Executes this filter.
     *
     * @param httpRequest Request for which this filter is invoked.
     * @param responseWriter Response writer.
     * @param executionContext Execution context for this execution.
     */
    void filter(FullHttpRequest httpRequest, HttpResponseWriter responseWriter, FilterExecutionContext executionContext);
}
