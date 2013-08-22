package com.netflix.karyon.server.http.filter;

import com.netflix.karyon.server.http.spi.HttpResponseWriter;
import io.netty.handler.codec.http.FullHttpRequest;

/**
 *
 * @author Nitesh Kant
 */
public interface FilterExecutionContext {

    void executeNextFilter(FullHttpRequest request, HttpResponseWriter responseWriter);
}
