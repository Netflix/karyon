package com.netflix.karyon.example.netty;

import com.netflix.karyon.server.http.HttpServerBuilder;
import com.netflix.karyon.server.http.filter.Filter;
import com.netflix.karyon.server.http.filter.FilterExecutionContext;
import com.netflix.karyon.server.http.jersey.blocking.JerseyRouterProvider;
import com.netflix.karyon.server.http.spi.HttpRequestRouter;
import com.netflix.karyon.server.http.spi.HttpResponseWriter;
import io.netty.channel.ChannelOption;
import io.netty.handler.codec.http.FullHttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Nitesh Kant
 */
public class Server {

    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    public static void main(String[] args) throws Exception {
        HttpRequestRouter router = JerseyRouterProvider.createRouter();

        HttpServerBuilder builder = new HttpServerBuilder(8099);

        builder.requestRouter(router, 10).blocking().withAcceptorThreads(1)
               .serverSocketOption(ChannelOption.SO_BACKLOG, 100)
               .clientSocketOption(ChannelOption.TCP_NODELAY, true)
               .filter("/*", new LoggingFilter())
               .build().start();
    }

    private static class LoggingFilter implements Filter {
        @Override
        public void filter(FullHttpRequest httpRequest, HttpResponseWriter responseWriter,
                           FilterExecutionContext executionContext) {
            logger.info("Filter invoked.");
            executionContext.executeNextFilter(httpRequest, responseWriter);
        }
    }
}
