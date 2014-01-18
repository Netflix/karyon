package com.netflix.karyon.server.http;

import com.netflix.karyon.server.http.interceptor.NextInterceptorInvoker;
import com.netflix.karyon.server.http.interceptor.OutboundInterceptor;
import com.netflix.karyon.server.http.interceptor.OutboundNextInterceptorInvoker;
import com.netflix.karyon.server.spi.ResponseWriter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.HttpObject;
import io.netty.util.internal.TypeParameterMatcher;

import java.util.List;

/**
 * A netty channel handler that invokes all the {@link OutboundInterceptor} configured for the pipeline.
 */
public class OutboundInterceptorNettyHandler<T extends HttpObject> extends ChannelOutboundHandlerAdapter {

    private final NextInterceptorInvoker<T, T> nextInvoker;
    private final ResponseWriter<T> responseWriter;
    private final TypeParameterMatcher matcher;

    public OutboundInterceptorNettyHandler(List<OutboundInterceptor<T>> interceptors,
                                           ResponseWriter<T> responseWriter) {
        matcher = TypeParameterMatcher.find(this, OutboundInterceptorNettyHandler.class, "T");
        this.responseWriter = responseWriter;
        nextInvoker = new OutboundNextInterceptorInvoker<T>(interceptors, new OutboundInterceptor<T>() {

            @Override
            public void interceptOut(T httpResponse, ResponseWriter<T> responseWriter,
                                     NextInterceptorInvoker<T, T> executionContext) {
                responseWriter.getChannelHandlerContext().write(httpResponse);
            }
        });
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (matcher.match(msg) && null != nextInvoker) {
            @SuppressWarnings("unchecked")
            T httpResponse = (T) msg;
            nextInvoker.executeNext(httpResponse, responseWriter);
        }
        super.write(ctx, msg, promise);
    }
}
