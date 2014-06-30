package com.netflix.karyon.transport.http;

import com.netflix.karyon.transport.interceptor.DuplexInterceptor;
import com.netflix.karyon.transport.interceptor.InboundInterceptor;
import com.netflix.karyon.transport.interceptor.InterceptorKey;
import com.netflix.karyon.transport.interceptor.InterceptorSupport;
import com.netflix.karyon.transport.interceptor.OutboundInterceptor;
import io.netty.handler.codec.http.HttpMethod;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;

import java.util.HashMap;
import java.util.Map;

/**
 * An extension of {@link InterceptorSupport} to add HTTP specific methods for attaching interceptors.
 *
 * @author Nitesh Kant
 */
public class HttpInterceptorSupport<I, O>
        extends InterceptorSupport<HttpServerRequest<I>, HttpServerResponse<O>, HttpKeyEvaluationContext> {

    private final Map<InterceptorKey<HttpServerRequest<I>, HttpKeyEvaluationContext>, HttpAttacher> attachers;

    public HttpInterceptorSupport() {
        attachers = new HashMap<InterceptorKey<HttpServerRequest<I>, HttpKeyEvaluationContext>, HttpAttacher>();
    }

    public HttpAttacher forUri(String uri) {
        if (null == uri || uri.isEmpty()) {
            throw new IllegalArgumentException("Uri can not be null or empty.");
        }
        return getAttacherForKey(new ServletStyleUriConstraintKey<I>(uri, ""));
    }

    public HttpAttacher forUriRegex(String uriRegEx) {
        if (null == uriRegEx || uriRegEx.isEmpty()) {
            throw new IllegalArgumentException("Uri regular expression can not be null or empty.");
        }
        return getAttacherForKey(new RegexUriConstraintKey<I>(uriRegEx));
    }

    public HttpAttacher forHttpMethod(HttpMethod method) {
        if (null == method) {
            throw new IllegalArgumentException("Uri can not be null or empty.");
        }
        return getAttacherForKey(new MethodConstraintKey<I>(method));
    }

    protected HttpAttacher getAttacherForKey(InterceptorKey<HttpServerRequest<I>, HttpKeyEvaluationContext> key) {
        HttpAttacher attacher = attachers.get(key);
        if (null == attacher) {
            attacher = new HttpAttacher(key);
            attachers.put(key, attacher);
        }
        return attacher;
    }

    @Override
    protected void _finish() {
        attachers.clear();
        super._finish();
    }

    public class HttpAttacher {

        private final Attacher delegate;

        public HttpAttacher(InterceptorKey<HttpServerRequest<I>, HttpKeyEvaluationContext> key) {
            delegate = new Attacher(key);
        }

        public HttpInterceptorSupport<I, O> intercept(InboundInterceptor<HttpServerRequest<I>, HttpServerResponse<O>>... interceptors) {
            delegate.intercept(interceptors);
            return HttpInterceptorSupport.this;
        }

        public HttpInterceptorSupport<I, O> intercept(OutboundInterceptor<HttpServerResponse<O>>... interceptors) {
            delegate.intercept(interceptors);
            return HttpInterceptorSupport.this;
        }

        public HttpInterceptorSupport<I, O> intercept(DuplexInterceptor<HttpServerRequest<I>, HttpServerResponse<O>>... interceptors) {
            delegate.intercept(interceptors);
            return HttpInterceptorSupport.this;
        }
    }
}
