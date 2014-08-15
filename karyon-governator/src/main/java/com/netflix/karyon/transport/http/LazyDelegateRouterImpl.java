package com.netflix.karyon.transport.http;

import com.google.inject.Injector;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import io.reactivex.netty.protocol.http.server.RequestHandler;
import rx.Observable;

/**
 * @author Nitesh Kant
 */
class LazyDelegateRouterImpl<I, O> implements LazyDelegateRouter<I, O> {

    private final GovernatorHttpInterceptorSupport<I, O> interceptorSupport;
    private RequestHandler<I, O> delegate;

    LazyDelegateRouterImpl(GovernatorHttpInterceptorSupport<I, O> interceptorSupport) {
        this.interceptorSupport = interceptorSupport;
        delegate = new RequestHandler<I, O>() {
            @Override
            public Observable<Void> handle(HttpServerRequest<I> request, HttpServerResponse<O> response) {
                response.setStatus(HttpResponseStatus.NOT_FOUND);
                return response.close();
            }
        };
    }

    @Override
    public Observable<Void> handle(HttpServerRequest<I> request, HttpServerResponse<O> response) {
        return delegate.handle(request, response);
    }

    @Override
    public void setRouter(Injector injector, RequestHandler<I, O> delegate) {
        interceptorSupport.finish(injector);
        this.delegate = delegate;
    }
}
