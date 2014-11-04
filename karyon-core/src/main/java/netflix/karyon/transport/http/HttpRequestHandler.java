package netflix.karyon.transport.http;

import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import io.reactivex.netty.protocol.http.server.RequestHandler;
import netflix.karyon.transport.interceptor.AbstractInterceptorSupport;
import netflix.karyon.transport.interceptor.InterceptorExecutor;
import rx.Observable;

/**
 * An implementation of {@link RequestHandler} for karyon.
 *
 * @author Nitesh Kant
 */
public class HttpRequestHandler<I, O> implements RequestHandler<I, O> {

    private final InterceptorExecutor<HttpServerRequest<I>, HttpServerResponse<O>, HttpKeyEvaluationContext> executor;

    public HttpRequestHandler(RequestHandler<I, O> router) {
        this(router, new HttpInterceptorSupport<I, O>());
    }

    public HttpRequestHandler(RequestHandler<I, O> router,
                              AbstractInterceptorSupport<HttpServerRequest<I>, HttpServerResponse<O>, HttpKeyEvaluationContext, ?, ?> interceptorSupport) {
        executor = new InterceptorExecutor<HttpServerRequest<I>, HttpServerResponse<O>, HttpKeyEvaluationContext>(interceptorSupport, router);
    }

    @Override
    public Observable<Void> handle(HttpServerRequest<I> request, HttpServerResponse<O> response) {
        return executor.execute(request, response, new HttpKeyEvaluationContext(response.getChannel()));
    }
}
