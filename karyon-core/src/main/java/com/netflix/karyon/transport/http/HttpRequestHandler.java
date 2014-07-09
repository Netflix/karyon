package com.netflix.karyon.transport.http;

import com.netflix.karyon.transport.interceptor.AbstractInterceptorSupport;
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

    private final InterceptorExecutor<HttpServerRequest<I>, HttpServerResponse<O>, HttpKeyEvaluationContext> executor;

    public HttpRequestHandler(HttpRequestRouter<I, O> router) {
        this(router, new HttpInterceptorSupport<I, O>());
    }

    public HttpRequestHandler(HttpRequestRouter<I, O> router,
                              AbstractInterceptorSupport<HttpServerRequest<I>, HttpServerResponse<O>, HttpKeyEvaluationContext, ?, ?> interceptorSupport) {
        executor = new InterceptorExecutor<HttpServerRequest<I>, HttpServerResponse<O>, HttpKeyEvaluationContext>(interceptorSupport, router);
    }

    @Override
    public Observable<Void> handle(HttpServerRequest<I> request, HttpServerResponse<O> response) {
        return executor.execute(request, response, new HttpKeyEvaluationContext(response.getChannelHandlerContext()));
    }
}
