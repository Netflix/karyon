package netflix.karyon.transport.http;

import io.netty.handler.codec.http.HttpMethod;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.RequestHandler;
import netflix.karyon.transport.interceptor.InterceptorKey;

/**
 * A convenience builder to create {@link HttpRequestHandler} instances.
 *
 * @author Nitesh Kant
 */
public class HttpRequestHandlerBuilder<I, O> {

    private final HttpInterceptorSupport<I, O> interceptorSupport;
    private final RequestHandler<I, O> router;

    public HttpRequestHandlerBuilder(RequestHandler<I, O> router) {
        this(new HttpInterceptorSupport<I, O>(), router);
    }

    public HttpRequestHandlerBuilder(HttpInterceptorSupport<I, O> interceptorSupport,
                                     RequestHandler<I, O> router) {
        this.interceptorSupport = interceptorSupport;
        this.router = router;
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

    public RequestHandler<I, O> getRouter() {
        return router;
    }

    public HttpRequestHandler<I, O> build() {
        interceptorSupport.finish();
        return new HttpRequestHandler<I, O>(router, interceptorSupport);
    }
}
