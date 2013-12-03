package com.netflix.karyon.example.netty;

import com.netflix.karyon.server.http.BlockingHttpServerBuilder;
import com.netflix.karyon.server.http.NonBlockingHttpServerBuilder;
import com.netflix.karyon.server.http.jersey.blocking.JerseyRouterProvider;
import com.netflix.karyon.server.http.spi.HttpRequestRouter;
import com.netflix.karyon.server.http.spi.HttpResponseWriter;
import io.netty.channel.ChannelOption;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Nitesh Kant
 */
public final class Server {

    public static void main(String[] args) throws Exception {
        HttpRequestRouter router = JerseyRouterProvider.createRouter();

        BlockingHttpServerBuilder builder = new BlockingHttpServerBuilder(8099);

        builder.requestRouter(router)
               .withWorkerCount(10)
               .serverSocketOption(ChannelOption.SO_BACKLOG, 100)
               .clientSocketOption(ChannelOption.TCP_NODELAY, true)
               .forUri("/hello/*").interceptWith(new InboundInterceptorImpl())
               .forUri("/hello/out/").interceptWith(new OutboundInterceptorImpl())
               .forUri("/*").interceptWith(new LoggingInterceptor())
               .build().startWithoutWaitingForShutdown();

        NonBlockingHttpServerBuilder nonBlockingBuilder = new NonBlockingHttpServerBuilder(8098);
        HttpRequestRouter customRouter = new AlwaysNotFoundRouter();
        nonBlockingBuilder.requestRouter(customRouter)
                          .serverSocketOption(ChannelOption.SO_BACKLOG, 100)
                          .clientSocketOption(ChannelOption.TCP_NODELAY, true)
                          .forUri("/*").interceptWith(new InboundInterceptorImpl())
                          .forUri("/*").interceptWith(new OutboundInterceptorImpl())
                          .forUri("/*").interceptWith(new LoggingInterceptor())
                          .build().start();
    }

    private static class AlwaysNotFoundRouter implements HttpRequestRouter {

        private ExecutorService executors = Executors.newCachedThreadPool();

        @Override
        public boolean isBlocking() {
            return false;
        }

        @Override
        public void process(FullHttpRequest request, final HttpResponseWriter responseWriter) {
            executors.submit(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    responseWriter.createResponse(HttpResponseStatus.NOT_FOUND, null);
                    responseWriter.sendResponse();
                    return null;
                }
            });
        }

        @Override
        public void start() {
            // No Op
        }
    }
}
