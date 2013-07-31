package com.netflix.karyon.server.netty;


import com.google.common.base.Preconditions;
import com.netflix.karyon.server.netty.spi.HttpRequestRouter;
import com.netflix.karyon.server.netty.spi.HttpResponseWriter;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;

import javax.annotation.Nullable;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;

public class ServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private final HttpRequestRouter httpRequestRouter;

    public ServerHandler(HttpRequestRouter httpRequestRouter) {
        super(true);
        Preconditions.checkNotNull(httpRequestRouter);
        this.httpRequestRouter = httpRequestRouter;
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final FullHttpRequest request) throws Exception {

        httpRequestRouter.process(request, new HttpResponseWriter() {

            private DefaultFullHttpResponse response;

            @Override
            public FullHttpResponse createResponse(HttpResponseStatus responseStatus, @Nullable ByteBuf content) {
                if (null == content) {
                    content = Unpooled.buffer(0);
                }
                response = new DefaultFullHttpResponse(request.getProtocolVersion(), responseStatus, content);
                return response;
            }

            @Override
            public void sendResponse() {
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

            @Override
            public ChannelHandlerContext getChannelHandlerContext() {
                return ctx;
            }
        });
    }

    @Override
    public void channelReadComplete(final ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }
}
