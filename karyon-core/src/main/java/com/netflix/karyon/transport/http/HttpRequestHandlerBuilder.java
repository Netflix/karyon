package com.netflix.karyon.transport.http;

import com.netflix.karyon.transport.interceptor.InterceptorKey;
import com.netflix.karyon.transport.interceptor.InterceptorSupport;
import io.netty.handler.codec.http.HttpMethod;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;

/**
 * A convenience builder to create {@link HttpRequestHandler} instances.
 *
 * @author Nitesh Kant
 */
public class HttpRequestHandlerBuilder<I, O> {

    private final HttpInterceptorSupport<I, O> interceptorSupport;
    private final HttpRequestRouter<I, O> router;

    public HttpRequestHandlerBuilder(HttpRequestRouter<I, O> router) {
        this.router = router;
        interceptorSupport = new HttpInterceptorSupport<I, O>();
    }

    public InterceptorSupport.Attacher forKey(InterceptorKey<HttpServerRequest<I>, HttpKeyEvaluationContext> key) {
        return interceptorSupport.forKey(key);
    }

    public HttpInterceptorSupport.HttpAttacher forUri(String uri) {
        return interceptorSupport.forUri(uri);
    }

    public HttpInterceptorSupport.HttpAttacher forUriRegex(String uriRegEx) {
        return interceptorSupport.forUriRegex(uriRegEx);
    }

    public HttpInterceptorSupport.HttpAttacher forHttpMethod(HttpMethod method) {
        return interceptorSupport.forHttpMethod(method);
    }

    public HttpRequestHandler<I, O> build() {
        return new HttpRequestHandler<I, O>(router, interceptorSupport);
    }
}
