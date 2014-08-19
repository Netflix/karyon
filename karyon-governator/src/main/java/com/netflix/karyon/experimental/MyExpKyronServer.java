package com.netflix.karyon.experimental;

import java.nio.charset.Charset;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.netflix.governator.annotations.Modules;
import com.netflix.karyon.experimental.MyExpKyronServer.MyHttpModuleA;
import com.netflix.karyon.experimental.MyExpKyronServer.MyHttpModuleB;
import com.netflix.karyon.experimental.MyExpKyronServer.MyTcpModule;
import com.netflix.karyon.transport.http.HttpRequestRouter;
import io.netty.buffer.ByteBuf;
import io.reactivex.netty.channel.ConnectionHandler;
import io.reactivex.netty.channel.ObservableConnection;
import io.reactivex.netty.protocol.http.server.HttpServer;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import io.reactivex.netty.server.RxServer;
import io.reactivex.netty.servo.ServoEventsListenerFactory;
import rx.Observable;

/**
 * @author Tomasz Bak
 */
@Singleton
@Modules(include = {MyHttpModuleA.class, MyHttpModuleB.class, MyTcpModule.class})
public class MyExpKyronServer extends ExpKyronServer {

    @Inject
    public MyExpKyronServer(@Named("httpServerA") HttpServer<ByteBuf, ByteBuf> serverA,
                            @Named("httpServerB") HttpServer<ByteBuf, ByteBuf> serverB,
                            @Named("tcpServer") RxServer<ByteBuf, ByteBuf> tcpServer) {
        super(serverA, serverB, tcpServer);
    }

    public static class MyHttpModuleA extends ExpHttpModule<ByteBuf, ByteBuf> {

        public MyHttpModuleA() {
            super("httpServerA", ByteBuf.class, ByteBuf.class);
        }

        @Override
        protected void configureServer() {
            bindRouter(MyRouteA.class);
            bindEventsListenerFactory(ServoEventsListenerFactory.class);
            server().port(8080).threadPoolSize(100).bind();
        }

        public static class MyRouteA implements HttpRequestRouter<ByteBuf, ByteBuf> {
            @Override
            public Observable<Void> route(HttpServerRequest<ByteBuf> request, HttpServerResponse<ByteBuf> response) {
                response.writeAndFlush(response.getAllocator().buffer().writeBytes("Hello and goodbye from A!!!".getBytes(Charset.defaultCharset())));
                return response.close();
            }
        }
    }

    public static class MyHttpModuleB extends ExpHttpModule<ByteBuf, ByteBuf> {

        public MyHttpModuleB() {
            super("httpServerB", ByteBuf.class, ByteBuf.class);
        }

        @Override
        protected void configureServer() {
            bindRouter(MyRouteB.class);
            bindEventsListenerFactory(ServoEventsListenerFactory.class);
            server().port(8081).threadPoolSize(100).bind();
        }

        public static class MyRouteB implements HttpRequestRouter<ByteBuf, ByteBuf> {
            @Override
            public Observable<Void> route(HttpServerRequest<ByteBuf> request, HttpServerResponse<ByteBuf> response) {
                response.writeAndFlush(response.getAllocator().buffer().writeBytes("Hello and goodbye from B!!!".getBytes(Charset.defaultCharset())));
                return response.close();
            }
        }
    }

    public static class MyTcpModule extends ExpTcpModule<ByteBuf, ByteBuf> {
        public MyTcpModule() {
            super("tcpServer", ByteBuf.class, ByteBuf.class);
        }

        @Override
        protected void configureServer() {
            bindConnectionHandler(MyConnectionHandler.class);
            bindEventsListenerFactory(ServoEventsListenerFactory.class);
            server().port(8082).bind();
        }

        public static class MyConnectionHandler implements ConnectionHandler<ByteBuf, ByteBuf> {
            @Override
            public Observable<Void> handle(ObservableConnection<ByteBuf, ByteBuf> connection) {
                return connection.writeAndFlush(connection.getAllocator().buffer().writeBytes("Hello and goodbye!!!".getBytes(Charset.defaultCharset())));
            }
        }
    }

    public static void main(String[] args) throws Exception {
        bootstrap(MyExpKyronServer.class);
    }
}
