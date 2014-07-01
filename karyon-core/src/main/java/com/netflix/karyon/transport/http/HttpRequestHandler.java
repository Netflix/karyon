package com.netflix.karyon.transport.http;

import com.netflix.karyon.transport.RequestRouter;
import com.netflix.karyon.transport.interceptor.InterceptorExecutor;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import io.reactivex.netty.protocol.http.server.RequestHandler;
import rx.Observable;

/**
 * An implementation of {@link RequestHandler} for karyon.
 *
 * @author Nitesh Kant
 */
public class HttpRequestHandler<I, O> implements RequestHandler<I, O> {

    private final RequestRouter<HttpServerRequest<I>, HttpServerResponse<O>> router;
    private final InterceptorExecutor<HttpServerRequest<I>, HttpServerResponse<O>, HttpKeyEvaluationContext> executor;

    public HttpRequestHandler(HttpRequestRouter<I, O> router) {
        this(router, new HttpInterceptorSupport<I, O>());
    }

    public HttpRequestHandler(HttpRequestRouter<I, O> router, HttpInterceptorSupport<I, O> interceptorSupport) {
        this.router = router;
        if (interceptorSupport.hasAtleastOneInterceptor()) {
            executor = new InterceptorExecutor<HttpServerRequest<I>, HttpServerResponse<O>, HttpKeyEvaluationContext>(
                    interceptorSupport, router);
        } else {
            executor = null;
        }
    }

    @Override
    public Observable<Void> handle(HttpServerRequest<I> request, HttpServerResponse<O> response) {
        if (null == executor) {
            return router.route(request, response);
        }
        return executor.execute(request, response, new HttpKeyEvaluationContext(response.getChannelHandlerContext()));
    }
}
