package com.netflix.karyon.server.http;

import com.netflix.karyon.server.http.interceptor.BidirectionalInterceptorAdapter;
import com.netflix.karyon.server.http.interceptor.InterceptorExecutionContextImpl;
import com.netflix.karyon.server.http.interceptor.OutboundInterceptor;
import com.netflix.karyon.server.http.spi.HttpResponseWriter;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author Nitesh Kant
 */
class InterceptorAwareResponseWriter implements HttpResponseWriter {

    private final InterceptorExecutionContextImpl ctxImpl;
    private final HttpResponseWriterImpl writerDelegate;

    InterceptorAwareResponseWriter(HttpResponseWriterImpl writerDelegate, List<OutboundInterceptor> outboundInterceptors,
                                   BidirectionalInterceptorAdapter terminatingInterceptor) {
        this.writerDelegate = writerDelegate;
        ctxImpl = new InterceptorExecutionContextImpl(outboundInterceptors, terminatingInterceptor);
    }

    @Override
    public FullHttpResponse createResponse(HttpResponseStatus responseStatus, @Nullable ByteBuf content) {
        return writerDelegate.createResponse(responseStatus, content);
    }

    @Override
    public void sendResponse() {
        writerDelegate.validateReadyForSend();
        ctxImpl.executeNextInterceptor(writerDelegate.getRequest(), this);
    }

    @Nullable
    @Override
    public FullHttpResponse response() {
        return writerDelegate.response();
    }

    @Override
    public boolean isResponseCreated() {
        return writerDelegate.isResponseCreated();
    }

    @Override
    public ChannelHandlerContext getChannelHandlerContext() {
        return writerDelegate.getChannelHandlerContext();
    }
}
