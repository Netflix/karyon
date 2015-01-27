package netflix.karyon.transport.http;

import io.netty.handler.codec.http.HttpMethod;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import netflix.karyon.transport.interceptor.AbstractAttacher;
import netflix.karyon.transport.interceptor.AbstractInterceptorSupport;
import netflix.karyon.transport.interceptor.InterceptorKey;

/**
 * An extension of {@link netflix.karyon.transport.interceptor.InterceptorSupport} to add HTTP specific methods for attaching interceptors.
 *
 * @author Nitesh Kant
 */
public class HttpInterceptorSupport<I, O> extends
        AbstractInterceptorSupport<HttpServerRequest<I>, HttpServerResponse<O>, HttpKeyEvaluationContext,
                                           HttpInterceptorSupport.HttpAttacher<I, O>, HttpInterceptorSupport<I, O>> {

    @Override
    protected HttpAttacher<I, O> newAttacher(InterceptorKey<HttpServerRequest<I>, HttpKeyEvaluationContext> key) {
        return new HttpAttacher<I, O>(this, key);
    }

    public HttpAttacher<I, O> forUri(String uri) {
        if (null == uri || uri.isEmpty()) {
            throw new IllegalArgumentException("Uri can not be null or empty.");
        }
        return getAttacherForKey(new ServletStyleUriConstraintKey<I>(uri, ""));
    }

    public HttpAttacher<I, O> forUriRegex(String uriRegEx) {
        if (null == uriRegEx || uriRegEx.isEmpty()) {
            throw new IllegalArgumentException("Uri regular expression can not be null or empty.");
        }
        return getAttacherForKey(new RegexUriConstraintKey<I>(uriRegEx));
    }

    public HttpAttacher<I, O> forHttpMethod(HttpMethod method) {
        if (null == method) {
            throw new IllegalArgumentException("Uri can not be null or empty.");
        }
        return getAttacherForKey(new MethodConstraintKey<I>(method));
    }

    public static class HttpAttacher<I, O>
            extends AbstractAttacher<HttpServerRequest<I>, HttpServerResponse<O>, HttpKeyEvaluationContext,
                                                 HttpInterceptorSupport<I, O>> {

        public HttpAttacher(HttpInterceptorSupport<I, O> support,
                            InterceptorKey<HttpServerRequest<I>, HttpKeyEvaluationContext> key) {
            super(support, key);
        }
    }
}
