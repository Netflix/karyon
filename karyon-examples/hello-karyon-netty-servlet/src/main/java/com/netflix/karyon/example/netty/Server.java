package com.netflix.karyon.example.netty;

import com.netflix.karyon.example.netty.servlet.HelloWorldServlet;
import com.netflix.karyon.example.netty.servlet.LoggingFilter;
import com.netflix.karyon.server.http.BlockingHttpServerBuilder;
import com.netflix.karyon.server.http.servlet.blocking.HTTPServletRequestRouterBuilder;
import com.netflix.karyon.server.http.spi.HttpRequestRouter;
import com.netflix.karyon.server.http.spi.HttpResponseWriter;
import io.netty.channel.ChannelOption;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * @author Nitesh Kant
 */
public final class Server {

    public static void main(String[] args) throws Exception {
        HttpRequestRouter router =
                new HTTPServletRequestRouterBuilder()
                        .contextPath("/hello-karyon-netty-servlet/REST/v1/")
                        .forUri("hello").serveWith(HelloWorldServlet.class)
                        .forUri("*").filterWith(LoggingFilter.class)
                        .build();

        BlockingHttpServerBuilder builder = new BlockingHttpServerBuilder(8099);

        builder.requestRouter(router)
               .withWorkerCount(10)
               .serverSocketOption(ChannelOption.SO_BACKLOG, 100)
               .clientSocketOption(ChannelOption.TCP_NODELAY, true)
               .build().start();
    }
}
