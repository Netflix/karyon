package com.netflix.karyon.server.http.interceptor;

import com.netflix.karyon.server.http.spi.HttpResponseWriter;
import io.netty.handler.codec.http.FullHttpRequest;

/**
 * An adapter to combine both {@link InboundInterceptor} and {@link OutboundInterceptor} thus providing an easier way
 * for interceptors that needs to intercept inbound and outbound processing. <br/>
 *
 * @see com.netflix.karyon.server.http.interceptor
 */
public abstract class BidirectionalInterceptorAdapter implements InboundInterceptor, OutboundInterceptor {

    public enum Direction {
        INBOUND,
        OUTBOUND
    }

    @Override
    public void interceptIn(FullHttpRequest httpRequest, HttpResponseWriter responseWriter,
                            InterceptorExecutionContext executionContext) {
        intercept(Direction.INBOUND, httpRequest, responseWriter, executionContext);
    }

    @Override
    public void interceptOut(FullHttpRequest httpRequest, HttpResponseWriter responseWriter,
                             InterceptorExecutionContext executionContext) {
        intercept(Direction.OUTBOUND, httpRequest, responseWriter, executionContext);
    }

    protected abstract void intercept(Direction direction, FullHttpRequest httpRequest,
                                      HttpResponseWriter responseWriter,
                                      InterceptorExecutionContext executionContext);
}
