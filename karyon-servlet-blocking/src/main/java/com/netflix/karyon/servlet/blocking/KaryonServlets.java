package com.netflix.karyon.servlet.blocking;

import com.netflix.karyon.transport.KaryonTransport;
import com.netflix.karyon.transport.http.HttpInterceptorSupport;
import com.netflix.karyon.transport.http.HttpRequestHandler;
import com.netflix.karyon.transport.http.HttpRequestHandlerBuilder;
import io.netty.buffer.ByteBuf;
import io.reactivex.netty.pipeline.PipelineConfigurators;
import io.reactivex.netty.protocol.http.server.HttpServer;
import io.reactivex.netty.protocol.http.server.HttpServerBuilder;

/**
 * @author Nitesh Kant
 */
public class KaryonServlets {

    public static HttpServer<ByteBuf, ByteBuf> fromDefaults(int port) {
        HttpServletRequestRouter router = new HTTPServletRequestRouterBuilder().build();
        return newServerBuilder(port, router);
    }

    public static HttpServer<ByteBuf, ByteBuf> from(int port, HttpServletRequestRouter router) {
        return newServerBuilder(port, router);
    }

    public static HttpServer<ByteBuf, ByteBuf> from(int port, HttpInterceptorSupport<ByteBuf, ByteBuf> interceptorSupport) {
        HttpServletRequestRouter router = new HTTPServletRequestRouterBuilder().build();
        return newServerBuilder(port, new HttpRequestHandlerBuilder<ByteBuf, ByteBuf>(interceptorSupport, router));
    }
    public static HttpServer<ByteBuf, ByteBuf> from(int port, HttpServletRequestRouter router,
                                                    HttpInterceptorSupport<ByteBuf, ByteBuf> interceptorSupport) {
        return newServerBuilder(port, new HttpRequestHandlerBuilder<ByteBuf, ByteBuf>(interceptorSupport, router));
    }

    protected static HttpServer<ByteBuf, ByteBuf> newServerBuilder(int port, HttpRequestHandlerBuilder<ByteBuf, ByteBuf> requestHandlerBuilder) {
        HttpRequestHandler<ByteBuf, ByteBuf> requestHandler = requestHandlerBuilder.build();
        HttpServerBuilder<ByteBuf, ByteBuf> builder = KaryonTransport.newHttpServerBuilder(port, requestHandler);
        return builder.pipelineConfigurator(PipelineConfigurators.<ByteBuf, ByteBuf>httpServerConfigurator()).build();
    }

    protected static HttpServer<ByteBuf, ByteBuf> newServerBuilder(int port, HttpServletRequestRouter router) {
        return KaryonTransport.newHttpServerBuilder(port, router)
                              .pipelineConfigurator(PipelineConfigurators.<ByteBuf, ByteBuf>httpServerConfigurator())
                              .build();
    }
}
