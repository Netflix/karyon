package com.netflix.karyon.server.http.servlet.blocking;

import com.netflix.karyon.server.http.spi.HttpResponseWriter;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import javax.annotation.Nullable;

/**
* @author Nitesh Kant
*/
class HttpResponseWriterMock implements HttpResponseWriter {

    private final HttpVersion version;
    @Nullable
    private final ChannelHandlerContext context;
    private FullHttpResponse response;
    private boolean responseSent;

    HttpResponseWriterMock(HttpVersion version, @Nullable ChannelHandlerContext context) {
        this.version = version;
        this.context = context;
    }

    @Override
    public FullHttpResponse createResponse(HttpResponseStatus responseStatus, @Nullable ByteBuf content) {
        response = new DefaultFullHttpResponse(version, responseStatus);
        return response;
    }

    @Override
    public void sendResponse() {
        // No op as it is a mock.
        responseSent = true;
    }

    @Nullable
    @Override
    public FullHttpResponse response() {
        return response;
    }

    @Override
    public boolean isResponseCreated() {
        return null != response;
    }

    @Override
    public boolean isResponseSent() {
        return responseSent;
    }

    @Override
    public ChannelHandlerContext getChannelHandlerContext() {
        return context;
    }
}
