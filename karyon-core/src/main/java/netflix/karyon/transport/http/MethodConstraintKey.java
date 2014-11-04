package netflix.karyon.transport.http;

import io.netty.handler.codec.http.HttpMethod;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
* @author Nitesh Kant
*/
public class MethodConstraintKey<I> implements HttpInterceptorKey<I> {

    private static final Logger logger = LoggerFactory.getLogger(MethodConstraintKey.class);

    private final HttpMethod method;

    public MethodConstraintKey(HttpMethod method) {
        if (null == method) {
            throw new NullPointerException("HTTP method in the interceptor constraint can not be null.");
        }
        this.method = method;
    }

    @Override
    public String toString() {
        return "MethodConstraintKey{" + "method=" + method + '}';
    }

    @Override
    public boolean apply(HttpServerRequest<I> request, HttpKeyEvaluationContext context) {
        boolean matches = request.getHttpMethod().equals(method);
        if (logger.isDebugEnabled()) {
            logger.debug("Result for HTTP method constraint for method {} and required method {} : {}",
                         request.getHttpMethod(), method, matches);
        }

        return matches;
    }
}
