package com.netflix.karyon.server.http;

import com.google.common.base.Preconditions;
import com.netflix.karyon.server.http.spi.HttpResponseWriter;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;

import javax.annotation.Nullable;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;

/**
 * @author Nitesh Kant
 */
public class HttpResponseWriterImpl implements HttpResponseWriter {

    private final FullHttpRequest request;
    private final ChannelHandlerContext ctx;
    private DefaultFullHttpResponse response;

    public HttpResponseWriterImpl(FullHttpRequest request, ChannelHandlerContext ctx) {
        this.request = request;
        this.ctx = ctx;
    }

    @Override
    public FullHttpResponse createResponse(HttpResponseStatus responseStatus, @Nullable ByteBuf content) {
        Preconditions.checkState(!isResponseCreated(), "Response already created.");

        if (null == content) {
            content = Unpooled.buffer(0);
        }
        response = new DefaultFullHttpResponse(request.getProtocolVersion(), responseStatus, content);
        return response;
    }

    @Override
    public void sendResponse() {
        validateReadyForSend();
        boolean keepAlive = HttpHeaders.isKeepAlive(request);
        if (keepAlive) {
            response.headers().set(CONTENT_LENGTH, response.content().readableBytes());
            response.headers().set(CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
        }

        ChannelFuture writeFuture = ctx.write(response);

        if (!keepAlive) {
            writeFuture.addListener(ChannelFutureListener.CLOSE);
        }
    }

    void validateReadyForSend() {
        Preconditions.checkState(null != response,
                                 "No response instance created. You must create a response before send.");
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
    public ChannelHandlerContext getChannelHandlerContext() {
        return ctx;
    }

    FullHttpRequest getRequest() {
        return request;
    }

    ChannelHandlerContext getCtx() {
        return ctx;
    }

    DefaultFullHttpResponse getResponse() {
        return response;
    }
}
