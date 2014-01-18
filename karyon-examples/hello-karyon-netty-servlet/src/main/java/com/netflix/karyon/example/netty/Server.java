package com.netflix.karyon.example.netty;

import com.netflix.karyon.example.netty.servlet.HelloWorldServlet;
import com.netflix.karyon.example.netty.servlet.LoggingFilter;
import com.netflix.karyon.server.http.BlockingHttpServerBuilder;
import com.netflix.karyon.server.http.FullHttpObjectPipelineConfiguratorImpl;
import com.netflix.karyon.server.http.HttpPipelineConfigurator;
import com.netflix.karyon.server.http.StatefulHttpResponseWriterImpl;
import com.netflix.karyon.server.http.servlet.blocking.HTTPServletRequestRouterBuilder;
import com.netflix.karyon.server.http.spi.HttpRequestRouter;
import com.netflix.karyon.server.spi.DefaultChannelPipelineConfigurator;
import io.netty.channel.ChannelOption;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;

/**
 * @author Nitesh Kant
 */
public final class Server {

    public static void main(String[] args) throws Exception {
        HttpRequestRouter<FullHttpRequest, FullHttpResponse> router =
                new HTTPServletRequestRouterBuilder()
                        .contextPath("/hello-karyon-netty-servlet/REST/v1/")
                        .forUri("hello").serveWith(HelloWorldServlet.class)
                        .forUri("*").filterWith(LoggingFilter.class)
                        .build();

        BlockingHttpServerBuilder<FullHttpRequest, FullHttpResponse> builder =
                new BlockingHttpServerBuilder<FullHttpRequest, FullHttpResponse>(8099);

        HttpPipelineConfigurator httpConfigurator = new HttpPipelineConfigurator();
        DefaultChannelPipelineConfigurator<FullHttpRequest, FullHttpResponse> pipelineConfigurator =
                new DefaultChannelPipelineConfigurator<FullHttpRequest, FullHttpResponse>(builder.getUniqueServerName(),
                                                                                          null,
                                                                                          new FullHttpObjectPipelineConfiguratorImpl(8192, httpConfigurator));
        builder.requestRouter(router)
               .withWorkerCount(10)
               .serverSocketOption(ChannelOption.SO_BACKLOG, 100)
               .clientSocketOption(ChannelOption.TCP_NODELAY, true)
               .pipelineConfigurator(pipelineConfigurator)
                .responseWriterFactory(new StatefulHttpResponseWriterImpl.ResponseWriterFactoryImpl())
               .build().start();
    }
}
