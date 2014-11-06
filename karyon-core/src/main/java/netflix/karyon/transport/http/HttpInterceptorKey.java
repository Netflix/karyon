package netflix.karyon.transport.http;

import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import netflix.karyon.transport.interceptor.InterceptorKey;

/**
 * @author Nitesh Kant
 */
public interface HttpInterceptorKey<I> extends InterceptorKey<HttpServerRequest<I>, HttpKeyEvaluationContext> {
}
