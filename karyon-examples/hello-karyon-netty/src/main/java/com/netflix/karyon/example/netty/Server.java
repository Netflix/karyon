package com.netflix.karyon.example.netty;

import com.netflix.karyon.server.http.BlockingHttpServerBuilder;
import com.netflix.karyon.server.http.NonBlockingHttpServerBuilder;
import com.netflix.karyon.server.http.interceptor.InboundInterceptor;
import com.netflix.karyon.server.http.interceptor.OutboundInterceptor;
import com.netflix.karyon.server.http.jersey.blocking.JerseyRouterProvider;
import com.netflix.karyon.server.http.spi.HttpRequestRouter;
import com.netflix.karyon.server.spi.ResponseWriter;
import io.netty.channel.ChannelOption;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.SucceededFuture;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Nitesh Kant
 */
public final class Server {

    public static void main(String[] args) throws Exception {
        HttpRequestRouter<FullHttpRequest, FullHttpResponse> router = JerseyRouterProvider.createRouter();

        BlockingHttpServerBuilder<FullHttpRequest, FullHttpResponse> builder =
                new BlockingHttpServerBuilder<FullHttpRequest, FullHttpResponse>(8099);

        LoggingInterceptor loggingInterceptor = new LoggingInterceptor();
        builder.requestRouter(router)
               .withWorkerCount(10)
               .serverSocketOption(ChannelOption.SO_BACKLOG, 100)
               .clientSocketOption(ChannelOption.TCP_NODELAY, true)
               .forUri("/hello/*").interceptWith(new InboundInterceptorImpl())
               .forUri("/hello/out/").interceptWith(new OutboundInterceptorImpl())
               .forUri("/*").interceptWith((InboundInterceptor<FullHttpRequest, FullHttpResponse>) loggingInterceptor)
               .forUri("/*").interceptWith((OutboundInterceptor<FullHttpResponse>) loggingInterceptor)
               .build().startWithoutWaitingForShutdown();

        NonBlockingHttpServerBuilder<FullHttpRequest, FullHttpResponse> nonBlockingBuilder =
                new NonBlockingHttpServerBuilder<FullHttpRequest, FullHttpResponse>(8098);
        HttpRequestRouter<FullHttpRequest, FullHttpResponse> customRouter = new AlwaysNotFoundRouter();
        nonBlockingBuilder.requestRouter(customRouter)
                          .serverSocketOption(ChannelOption.SO_BACKLOG, 100)
                          .clientSocketOption(ChannelOption.TCP_NODELAY, true)
                          .forUri("/*").interceptWith(new InboundInterceptorImpl())
                          .forUri("/*").interceptWith(new OutboundInterceptorImpl())
                          .forUri("/*").interceptWith((InboundInterceptor<FullHttpRequest, FullHttpResponse>) loggingInterceptor)
                          .forUri("/*").interceptWith((OutboundInterceptor<FullHttpResponse>) loggingInterceptor)
                          .build().start();
    }

    private static class AlwaysNotFoundRouter implements HttpRequestRouter<FullHttpRequest, FullHttpResponse> {

        private final ExecutorService executors = Executors.newCachedThreadPool();

        @Override
        public Future<Void> process(FullHttpRequest request, final ResponseWriter<FullHttpResponse> responseWriter) {
            executors.submit(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    responseWriter.write(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                                                                     HttpResponseStatus.NOT_FOUND));
                    return null;
                }
            });
            return new SucceededFuture<Void>(responseWriter.getChannelHandlerContext().executor(), null);
        }
    }
}
