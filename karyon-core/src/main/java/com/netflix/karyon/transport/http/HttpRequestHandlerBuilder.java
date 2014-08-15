package com.netflix.karyon.transport.http;

import com.netflix.karyon.transport.interceptor.InterceptorKey;
import io.netty.handler.codec.http.HttpMethod;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.RequestHandler;

/**
 * A convenience builder to create {@link HttpRequestHandler} instances.
 *
 * @author Nitesh Kant
 */
public class HttpRequestHandlerBuilder<I, O> {

    private final HttpInterceptorSupport<I, O> interceptorSupport;
    private final RequestHandler<I, O> handler;

    /**
     * Create a new builder with the given handler.
     * @param handler The handler to be applied at the end of the interceptor chain.
     */
    public HttpRequestHandlerBuilder(RequestHandler<I, O> handler) {
        this(new HttpInterceptorSupport<I, O>(), handler);
    }

    /**
     * Create a new builder with the given interceptor support and handler.
     * @param interceptorSupport
     * @param handler The handler to be applied at the end of the interceptor chain.
     */
    public HttpRequestHandlerBuilder(HttpInterceptorSupport<I, O> interceptorSupport,
                                     RequestHandler<I, O> handler) {
        this.interceptorSupport = interceptorSupport;
        this.handler = handler;
    }

    public HttpInterceptorSupport.HttpAttacher<I, O> forKey(InterceptorKey<HttpServerRequest<I>, HttpKeyEvaluationContext> key) {
        return interceptorSupport.forKey(key);
    }

    public HttpInterceptorSupport.HttpAttacher<I, O> forUri(String uri) {
        return interceptorSupport.forUri(uri);
    }

    public HttpInterceptorSupport.HttpAttacher<I, O> forUriRegex(String uriRegEx) {
        return interceptorSupport.forUriRegex(uriRegEx);
    }

    public HttpInterceptorSupport.HttpAttacher<I, O> forHttpMethod(HttpMethod method) {
        return interceptorSupport.forHttpMethod(method);
    }

    public HttpInterceptorSupport<I, O> getInterceptorSupport() {
        return interceptorSupport;
    }

    public RequestHandler<I, O> getHandler() {
        return handler;
    }

    public HttpRequestHandler<I, O> build() {
        interceptorSupport.finish();
        return new HttpRequestHandler<I, O>(handler, interceptorSupport);
    }
}
