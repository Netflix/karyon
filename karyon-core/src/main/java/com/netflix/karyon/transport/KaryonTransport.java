package com.netflix.karyon.transport;

import com.netflix.karyon.transport.http.HttpRequestHandler;
import com.netflix.karyon.transport.http.HttpRequestRouter;
import io.netty.buffer.ByteBuf;
import io.reactivex.netty.contexts.RxContexts;
import io.reactivex.netty.protocol.http.server.HttpServer;
import io.reactivex.netty.protocol.http.server.HttpServerBuilder;

/**
 * A factory class for creating karyon transport servers which are created using
 * <a href="https://github.com/Netflix/RxNetty">RxNetty</a>
 *
 * @author Nitesh Kant
 */
public final class KaryonTransport {

    public static final String DEFAULT_REQUEST_ID_CTX_KEY = "X-Karyon-REQUEST_ID";

    static {
        RxContexts.useRequestIdContextKey(DEFAULT_REQUEST_ID_CTX_KEY);
    }

    private KaryonTransport() {
    }

    public static HttpServerBuilder<ByteBuf, ByteBuf> newHttpServerBuilder(int port, HttpRequestRouter<ByteBuf, ByteBuf> router) {
        return RxContexts.newHttpServerBuilder(port, new HttpRequestHandler<ByteBuf, ByteBuf>(router),
                                               RxContexts.DEFAULT_CORRELATOR /*TODO: Use the specific correlator*/);
    }

    public static HttpServerBuilder<ByteBuf, ByteBuf> newHttpServerBuilder(int port,
                                                                           HttpRequestHandler<ByteBuf, ByteBuf> requestHandler) {
        return RxContexts.newHttpServerBuilder(port, requestHandler,
                                               RxContexts.DEFAULT_CORRELATOR /*TODO: Use the specific correlator*/);
    }

    public static HttpServer<ByteBuf, ByteBuf> newHttpServer(int port, HttpRequestRouter<ByteBuf, ByteBuf> router) {
        return newHttpServerBuilder(port, router).build();
    }

    public static HttpServer<ByteBuf, ByteBuf> newHttpServer(int port, HttpRequestHandler<ByteBuf, ByteBuf> requestHandler) {
        return newHttpServerBuilder(port, requestHandler).build();
    }

    /**
     * Karyon
     *
     * @param name The name of the context key to be used as default.
     */
    public static void useRequestIdContextKey(String name) {
        RxContexts.useRequestIdContextKey(name);
    }
}
