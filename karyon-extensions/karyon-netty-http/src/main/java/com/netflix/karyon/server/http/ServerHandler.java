package com.netflix.karyon.server.http;

import com.google.common.base.Preconditions;
import com.netflix.karyon.server.http.interceptor.BidirectionalInterceptorAdapter;
import com.netflix.karyon.server.http.interceptor.InboundInterceptor;
import com.netflix.karyon.server.http.interceptor.InterceptorExecutionContext;
import com.netflix.karyon.server.http.interceptor.InterceptorExecutionContextImpl;
import com.netflix.karyon.server.http.interceptor.OutboundInterceptor;
import com.netflix.karyon.server.http.spi.HttpRequestRouter;
import com.netflix.karyon.server.http.interceptor.PipelineFactory;
import com.netflix.karyon.server.http.spi.HttpResponseWriter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import java.util.List;

public class ServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private final HttpRequestRouter httpRequestRouter;

    @Nullable
    private final PipelineFactory interceptorPipelineFactory;

    public ServerHandler(@NotNull HttpRequestRouter httpRequestRouter, @Nullable PipelineFactory interceptorPipelineFactory) {
        super(true);
        Preconditions.checkNotNull(httpRequestRouter, "Request router can not be null.");
        this.interceptorPipelineFactory = interceptorPipelineFactory;
        this.httpRequestRouter = httpRequestRouter;
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final FullHttpRequest request) throws Exception {
        // Here we try to avoid unnecessary indirections of the interceptor ctx so we do not return a ctx with empty
        // pipeline as such.

        InterceptorExecutionContext interceptorExecutionContext = null;

        HttpResponseWriter writer = new HttpResponseWriterImpl(request, ctx);

        if (null != interceptorPipelineFactory) {
            RouterInterceptorAdapter terminatingInterceptor = null;

            List<OutboundInterceptor> outboundInterceptors = interceptorPipelineFactory.getOutboundInterceptors(request);
            List<InboundInterceptor> inboundInterceptors = interceptorPipelineFactory.getInboundInterceptors(request);

            if (!outboundInterceptors.isEmpty() || !inboundInterceptors.isEmpty()) {

                if (!outboundInterceptors.isEmpty()) {
                    terminatingInterceptor = new RouterInterceptorAdapter(httpRequestRouter,
                                                                          (HttpResponseWriterImpl) writer);
                    writer = new InterceptorAwareResponseWriter((HttpResponseWriterImpl) writer, outboundInterceptors,
                                                                terminatingInterceptor);
                }
                if (!inboundInterceptors.isEmpty()) {
                    if (null == terminatingInterceptor) {
                        terminatingInterceptor = new RouterInterceptorAdapter(httpRequestRouter, null); // If out interceptors are empty, this interceptor is just one way & hence does not need the writer.
                    }
                    interceptorExecutionContext = new InterceptorExecutionContextImpl(inboundInterceptors,
                                                                                      terminatingInterceptor);
                }
            }

        }

        if (null != interceptorExecutionContext) {
            interceptorExecutionContext.executeNextInterceptor(request, writer);
        } else {
            httpRequestRouter.process(request, writer);
        }
    }

    @Override
    public void channelReadComplete(final ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    private static class RouterInterceptorAdapter extends BidirectionalInterceptorAdapter {

        private final HttpRequestRouter router;

        @Nullable
        private final HttpResponseWriter writer;

        private RouterInterceptorAdapter(HttpRequestRouter router, @Nullable HttpResponseWriterImpl writer) {
            this.router = router;
            this.writer = writer;
        }

        @Override
        protected void intercept(Direction direction, FullHttpRequest httpRequest,
                                 HttpResponseWriter responseWriter, InterceptorExecutionContext executionContext) {
            switch (direction) {
                case INBOUND:
                    router.process(httpRequest, responseWriter);
                    break;
                case OUTBOUND:
                    if (null != responseWriter) {
                        writer.sendResponse();
                    }
                    break;
            }
        }
    }
}
