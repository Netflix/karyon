package com.netflix.karyon.example.netty;

import com.netflix.karyon.server.http.HttpServerBuilder;
import com.netflix.karyon.server.http.interceptor.Interceptor;
import com.netflix.karyon.server.http.interceptor.InterceptorExecutionContext;
import com.netflix.karyon.server.http.jersey.blocking.JerseyRouterProvider;
import com.netflix.karyon.server.http.spi.HttpRequestRouter;
import com.netflix.karyon.server.http.spi.HttpResponseWriter;
import io.netty.channel.ChannelOption;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Nitesh Kant
 */
public final class Server {

    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    public static void main(String[] args) throws Exception {
        HttpRequestRouter router = JerseyRouterProvider.createRouter();

        HttpServerBuilder builder = new HttpServerBuilder(8099);

        builder.requestRouter(router, 10)
               .serverSocketOption(ChannelOption.SO_BACKLOG, 100)
               .clientSocketOption(ChannelOption.TCP_NODELAY, true)
               .forUri("/*").interceptWith(new LoggingInterceptor())
               .forHttpMethod(HttpMethod.GET).interceptWith(new LoggingInterceptor())
               .build().start();
    }

    private static class LoggingInterceptor implements Interceptor {

        private static int count;
        private final int id;

        private LoggingInterceptor() {
            id = ++count;
        }

        @Override
        public void filter(FullHttpRequest httpRequest, HttpResponseWriter responseWriter,
                           InterceptorExecutionContext executionContext) {
            logger.info("Logging interceptor with id {} invoked.", id);
            executionContext.executeNextInterceptor(httpRequest, responseWriter);
        }
    }
}
