package netflix.karyon.transport;

import io.reactivex.netty.contexts.RxContexts;
import io.reactivex.netty.protocol.http.server.HttpServer;
import io.reactivex.netty.protocol.http.server.HttpServerBuilder;
import io.reactivex.netty.protocol.http.server.RequestHandler;
import netflix.karyon.transport.http.HttpRequestHandler;

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

    public static <I, O> HttpServerBuilder<I, O> newHttpServerBuilder(int port, RequestHandler<I, O> router) {
        return RxContexts.newHttpServerBuilder(port, new HttpRequestHandler<I, O>(router), RxContexts.DEFAULT_CORRELATOR);
    }

    public static <I, O> HttpServerBuilder<I, O> newHttpServerBuilder(int port, HttpRequestHandler<I, O> requestHandler) {
        return RxContexts.newHttpServerBuilder(port, requestHandler, RxContexts.DEFAULT_CORRELATOR);
    }

    public static <I, O> HttpServer<I, O> newHttpServer(int port, RequestHandler<I, O> router) {
        return newHttpServerBuilder(port, router).build();
    }

    public static <I, O> HttpServer<I, O> newHttpServer(int port, HttpRequestHandler<I, O> requestHandler) {
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
