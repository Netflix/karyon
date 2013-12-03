package com.netflix.karyon.server.http.interceptor;

import com.netflix.karyon.server.http.spi.HttpResponseWriter;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;

import javax.annotation.Nullable;

/**
* @author Nitesh Kant
*/
class HttpResponseWriterMock implements HttpResponseWriter {
    @Override
    public FullHttpResponse createResponse(HttpResponseStatus responseStatus, @Nullable ByteBuf content) {
        return null;
    }

    @Override
    public void sendResponse() {
    }

    @Nullable
    @Override
    public FullHttpResponse response() {
        return null;
    }

    @Override
    public boolean isResponseCreated() {
        return false;
    }

    @Override
    public boolean isResponseSent() {
        return false;
    }

    @Override
    public ChannelHandlerContext getChannelHandlerContext() {
        return null;
    }
}
