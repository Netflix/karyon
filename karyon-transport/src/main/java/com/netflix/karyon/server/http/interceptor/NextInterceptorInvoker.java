package com.netflix.karyon.server.http.interceptor;

import com.netflix.karyon.server.spi.ResponseWriter;
import io.netty.handler.codec.http.HttpObject;

/**
 * A convenience for {@link InboundInterceptor} and {@link OutboundInterceptor} to invoke the next interceptor in the
 * pipeline.
 *
 * @author Nitesh Kant
 */
public interface NextInterceptorInvoker<T extends HttpObject, O extends HttpObject> {

    /**
     * Executes the next interceptor if present, otherwise sends the request further in the netty pipeline.
     *
     * @param httpMessage Request/Response object based on whether it is an {@link InboundInterceptor} or
     * {@link OutboundInterceptor} respectively.
     * @param responseWriter Response writer.
     *
     * @see com.netflix.karyon.server.http.interceptor
     */
    void executeNext(T httpMessage, ResponseWriter<O> responseWriter);
}
