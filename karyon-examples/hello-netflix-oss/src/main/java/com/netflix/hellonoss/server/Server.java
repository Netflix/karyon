package com.netflix.hellonoss.server;

import com.netflix.karyon.transport.http.HttpRequestRouter;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import rx.Observable;

/**
 * @author Nitesh Kant
 */
public final class Server {

    public static void main(String[] args) throws Exception {
//        HttpRequestRouter<FullHttpRequest, FullHttpResponse> router = JerseyRouterProvider.createRouter();
//
//        BlockingHttpServerBuilder<FullHttpRequest, FullHttpResponse> builder =
//                new BlockingHttpServerBuilder<FullHttpRequest, FullHttpResponse>(8099);
//
//        LoggingInterceptor loggingInterceptor = new LoggingInterceptor();
//        builder.requestRouter(router)
//               .withWorkerCount(10)
//               .serverSocketOption(ChannelOption.SO_BACKLOG, 100)
//               .clientSocketOption(ChannelOption.TCP_NODELAY, true)
//               .karyonBootstrap()
//               .forUri("/hello/*").interceptWith(new InboundInterceptorImpl())
//               .forUri("/hello/out/").interceptWith(new OutboundInterceptorImpl())
//               .forUri("/*").interceptWith((InboundInterceptor<FullHttpRequest, FullHttpResponse>) loggingInterceptor)
//               .forUri("/*").interceptWith((OutboundInterceptor<FullHttpResponse>) loggingInterceptor)
//               .build().startWithoutWaitingForShutdown();
//
//        NonBlockingHttpServerBuilder<FullHttpRequest, FullHttpResponse> nonBlockingBuilder =
//                new NonBlockingHttpServerBuilder<FullHttpRequest, FullHttpResponse>(8098);
//        HttpRequestRouter<FullHttpRequest, FullHttpResponse> customRouter = new AlwaysNotFoundRouter();
//        nonBlockingBuilder.requestRouter(customRouter)
//                          .serverSocketOption(ChannelOption.SO_BACKLOG, 100)
//                          .clientSocketOption(ChannelOption.TCP_NODELAY, true)
//                          .forUri("/*").interceptWith(new InboundInterceptorImpl())
//                          .forUri("/*").interceptWith(new OutboundInterceptorImpl())
//                          .forUri("/*").interceptWith((InboundInterceptor<FullHttpRequest, FullHttpResponse>) loggingInterceptor)
//                          .forUri("/*").interceptWith((OutboundInterceptor<FullHttpResponse>) loggingInterceptor)
//                          .build().start();
    }

    private static class AlwaysNotFoundRouter implements HttpRequestRouter<ByteBuf, ByteBuf> {

        @Override
        public Observable<Void> route(HttpServerRequest<ByteBuf> request, HttpServerResponse<ByteBuf> response) {
            response.setStatus(HttpResponseStatus.NOT_FOUND);
            return response.close();
        }
    }
}
