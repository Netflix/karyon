package com.netflix.karyon.server.http;


import com.google.common.base.Preconditions;
import com.netflix.karyon.server.http.interceptor.Interceptor;
import com.netflix.karyon.server.http.interceptor.InterceptorExecutionContext;
import com.netflix.karyon.server.http.interceptor.InterceptorExecutionContextImpl;
import com.netflix.karyon.server.http.spi.HttpRequestRouter;
import com.netflix.karyon.server.http.interceptor.PipelineFactory;
import com.netflix.karyon.server.http.spi.HttpResponseWriter;
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
import javax.validation.constraints.NotNull;

import java.util.List;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;

public class ServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private final HttpRequestRouter httpRequestRouter;

    @Nullable
    private final PipelineFactory filterPipelineFactory;
    @Nullable
    private final Interceptor terminatingInterceptor;

    public ServerHandler(@NotNull HttpRequestRouter httpRequestRouter, @Nullable PipelineFactory filterPipelineFactory) {
        super(true);
        Preconditions.checkNotNull(httpRequestRouter, "Request router can not be null.");
        this.filterPipelineFactory = filterPipelineFactory;
        this.httpRequestRouter = httpRequestRouter;
        if (null != filterPipelineFactory) {
            terminatingInterceptor = new RouterInterceptorAdapter(this.httpRequestRouter);
        } else {
            terminatingInterceptor = null;
        }
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final FullHttpRequest request) throws Exception {
        InterceptorExecutionContext interceptorExecutionContext = getFilterExecutionContext(request);
        HttpResponseWriterImpl responseWriter = new HttpResponseWriterImpl(request, ctx);

        if (null != interceptorExecutionContext) {
            interceptorExecutionContext.executeNextInterceptor(request, responseWriter);
        } else {
            httpRequestRouter.process(request, responseWriter);
        }
    }

    @Nullable
    private InterceptorExecutionContext getFilterExecutionContext(FullHttpRequest request) {
        if (null == filterPipelineFactory) {
            return null;
        }

        List<Interceptor> interceptors = filterPipelineFactory.getInterceptors(request);
        if (null == interceptors || interceptors.isEmpty()) {
            return null;
        }

        return new InterceptorExecutionContextImpl(interceptors, terminatingInterceptor);
    }

    @Override
    public void channelReadComplete(final ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    private static class HttpResponseWriterImpl implements HttpResponseWriter {

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
            Preconditions.checkState(null != response,
                                     "No response instance created. You must create a response before send.");
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
    }

    private static class RouterInterceptorAdapter implements Interceptor {

        private final HttpRequestRouter router;

        private RouterInterceptorAdapter(HttpRequestRouter router) {
            this.router = router;
        }

        @Override
        public void filter(FullHttpRequest httpRequest, HttpResponseWriter responseWriter,
                           InterceptorExecutionContext executionContext) {
            router.process(httpRequest, responseWriter);
        }
    }
}
