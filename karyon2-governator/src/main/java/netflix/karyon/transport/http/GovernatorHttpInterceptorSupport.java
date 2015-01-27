package netflix.karyon.transport.http;

import com.google.inject.Injector;
import io.netty.handler.codec.http.HttpMethod;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import netflix.karyon.transport.interceptor.AbstractAttacher;
import netflix.karyon.transport.interceptor.AbstractInterceptorSupport;
import netflix.karyon.transport.interceptor.DuplexInterceptor;
import netflix.karyon.transport.interceptor.InboundInterceptor;
import netflix.karyon.transport.interceptor.InterceptorHolder;
import netflix.karyon.transport.interceptor.InterceptorKey;
import netflix.karyon.transport.interceptor.OutboundInterceptor;
import rx.functions.Action1;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Nitesh Kant
 */
public class GovernatorHttpInterceptorSupport<I, O> extends
        AbstractInterceptorSupport<HttpServerRequest<I>, HttpServerResponse<O>, HttpKeyEvaluationContext,
                                   GovernatorHttpInterceptorSupport.GovernatorHttpAttacher<I, O>,
                                   GovernatorHttpInterceptorSupport<I, O>> {

    protected final List<HttpInClassHolder<I, O>> inboundInterceptorClasses;
    protected final List<HttpOutClassHolder<I, O>> outboundInterceptorClasses;
    private Action1<GovernatorHttpInterceptorSupport<I, O>> finishListener;

    public GovernatorHttpInterceptorSupport() {
        inboundInterceptorClasses = new ArrayList<HttpInClassHolder<I, O>>();
        outboundInterceptorClasses = new ArrayList<HttpOutClassHolder<I, O>>();
    }

    @Override
    protected GovernatorHttpAttacher<I, O> newAttacher(InterceptorKey<HttpServerRequest<I>, HttpKeyEvaluationContext> key) {
        return new GovernatorHttpAttacher<I, O>(this, key);
    }

    public GovernatorHttpAttacher<I, O> forUri(String uri) {
        if (null == uri || uri.isEmpty()) {
            throw new IllegalArgumentException("Uri can not be null or empty.");
        }
        return getAttacherForKey(new ServletStyleUriConstraintKey<I>(uri, ""));
    }

    public GovernatorHttpAttacher<I, O> forUriRegex(String uriRegEx) {
        if (null == uriRegEx || uriRegEx.isEmpty()) {
            throw new IllegalArgumentException("Uri regular expression can not be null or empty.");
        }
        return getAttacherForKey(new RegexUriConstraintKey<I>(uriRegEx));
    }

    public GovernatorHttpAttacher<I, O> forHttpMethod(HttpMethod method) {
        if (null == method) {
            throw new IllegalArgumentException("Uri can not be null or empty.");
        }
        return getAttacherForKey(new MethodConstraintKey<I>(method));
    }

    public GovernatorHttpInterceptorSupport<I, O> finish(Injector injector) {
        if (!finished) {
            for (HttpInClassHolder<I, O> holder : inboundInterceptorClasses) {
                HttpInboundHolder<I, O> ins = new HttpInboundHolder<I, O>(holder.getKey());
                for (Class<? extends InboundInterceptor<HttpServerRequest<I>, HttpServerResponse<O>>> interceptor : holder
                        .getInterceptors()) {
                    ins.addIn(injector.getInstance(interceptor));
                }
                inboundInterceptors.add(ins.buildHolder());
            }
            for (HttpOutClassHolder<I, O> holder : outboundInterceptorClasses) {
                HttpOutboundHolder<I, O> outs = new HttpOutboundHolder<I, O>(holder.getKey());
                for (Class<? extends OutboundInterceptor<HttpServerResponse<O>>> interceptor : holder.getInterceptors()) {
                    outs.addOut(injector.getInstance(interceptor));
                }
                outboundInterceptors.add(outs.buildHolder());
            }
            _finish();
            finished = true;
            if (null != finishListener) {
                finishListener.call(this);
            }
        }
        return this;
    }

    public void setFinishListener(Action1<GovernatorHttpInterceptorSupport<I, O>> finishListener) {
        this.finishListener = finishListener;
    }

    public static class GovernatorHttpAttacher<I, O>
            extends AbstractAttacher<HttpServerRequest<I>, HttpServerResponse<O>, HttpKeyEvaluationContext,
            GovernatorHttpInterceptorSupport<I, O>> {

        public GovernatorHttpAttacher(GovernatorHttpInterceptorSupport<I, O> support,
                                      InterceptorKey<HttpServerRequest<I>, HttpKeyEvaluationContext> key) {
            super(support, key);
        }

        public GovernatorHttpInterceptorSupport<I, O> interceptIn(Class<? extends InboundInterceptor<HttpServerRequest<I>, HttpServerResponse<O>>> interceptor) {
            ArrayList<Class<? extends InboundInterceptor<HttpServerRequest<I>, HttpServerResponse<O>>>> interceptors =
                    new ArrayList<Class<? extends InboundInterceptor<HttpServerRequest<I>, HttpServerResponse<O>>>>();
            interceptors.add(interceptor);
            return interceptIn(interceptors);
        }

        public GovernatorHttpInterceptorSupport<I, O> interceptIn(
                List<Class<? extends InboundInterceptor<HttpServerRequest<I>, HttpServerResponse<O>>>> interceptors) {
            HttpInClassHolder<I, O> holder = new HttpInClassHolder<I, O>(key, interceptors);
            interceptorSupport.inboundInterceptorClasses.add(holder);
            return interceptorSupport;
        }

        public GovernatorHttpInterceptorSupport<I, O> interceptOut(Class<? extends OutboundInterceptor<HttpServerResponse<O>>> interceptor) {
            ArrayList<Class<? extends OutboundInterceptor<HttpServerResponse<O>>>> interceptors =
                    new ArrayList<Class<? extends OutboundInterceptor<HttpServerResponse<O>>>>();
            interceptors.add(interceptor);
            return interceptOut(interceptors);
        }

        public GovernatorHttpInterceptorSupport<I, O> interceptOut(List<Class<? extends OutboundInterceptor<HttpServerResponse<O>>>> interceptors) {
            HttpOutClassHolder<I, O> holder = new HttpOutClassHolder<I, O>(key, interceptors);
            interceptorSupport.outboundInterceptorClasses.add(holder);
            return interceptorSupport;
        }

        @SuppressWarnings("unchecked")
        public GovernatorHttpInterceptorSupport<I, O> intercept(Class<? extends DuplexInterceptor<HttpServerRequest<I>, HttpServerResponse<O>>> interceptor) {
            ArrayList<Class<? extends DuplexInterceptor<HttpServerRequest<I>, HttpServerResponse<O>>>> interceptors =
                    new ArrayList<Class<? extends DuplexInterceptor<HttpServerRequest<I>, HttpServerResponse<O>>>>();
            interceptors.add(interceptor);
            return intercept(interceptors);
        }

        public GovernatorHttpInterceptorSupport<I, O> intercept(List<Class<? extends DuplexInterceptor<HttpServerRequest<I>, HttpServerResponse<O>>>> interceptors) {
            ArrayList<Class<? extends InboundInterceptor<HttpServerRequest<I>, HttpServerResponse<O>>>> ins =
                    new ArrayList<Class<? extends InboundInterceptor<HttpServerRequest<I>, HttpServerResponse<O>>>>();
            ArrayList<Class<? extends OutboundInterceptor<HttpServerResponse<O>>>> outs =
                    new ArrayList<Class<? extends OutboundInterceptor<HttpServerResponse<O>>>>();

            for (Class<? extends DuplexInterceptor<HttpServerRequest<I>, HttpServerResponse<O>>> interceptor : interceptors) {
                ins.add(interceptor);
                outs.add(interceptor);
            }

            HttpInClassHolder<I, O> inHolder = new HttpInClassHolder<I, O>(key, ins);
            interceptorSupport.inboundInterceptorClasses.add(inHolder);

            HttpOutClassHolder<I, O> outHolder = new HttpOutClassHolder<I, O>(key, outs);
            interceptorSupport.outboundInterceptorClasses.add(outHolder);
            return interceptorSupport;
        }
    }

    private static class HttpInboundHolder<I, O>  {

        private final InterceptorKey<HttpServerRequest<I>, HttpKeyEvaluationContext> key;
        private final List<InboundInterceptor<HttpServerRequest<I>, HttpServerResponse<O>>> interceptors;

        private HttpInboundHolder(InterceptorKey<HttpServerRequest<I>, HttpKeyEvaluationContext> key) {
            this.key = key;
            interceptors = new ArrayList<InboundInterceptor<HttpServerRequest<I>, HttpServerResponse<O>>>();
        }

        private void addIn(InboundInterceptor<HttpServerRequest<I>, HttpServerResponse<O>> interceptor) {
            interceptors.add(interceptor);
        }

        private InterceptorHolder<HttpServerRequest<I>, HttpKeyEvaluationContext, InboundInterceptor<HttpServerRequest<I>, HttpServerResponse<O>>> buildHolder() {
            return new InterceptorHolder<HttpServerRequest<I>, HttpKeyEvaluationContext, InboundInterceptor<HttpServerRequest<I>, HttpServerResponse<O>>>(key, interceptors);
        }
    }

    private static class HttpOutboundHolder<I, O>  {

        private final InterceptorKey<HttpServerRequest<I>, HttpKeyEvaluationContext> key;
        private final List<OutboundInterceptor<HttpServerResponse<O>>> interceptors;

        private HttpOutboundHolder(InterceptorKey<HttpServerRequest<I>, HttpKeyEvaluationContext> key) {
            this.key = key;
            interceptors = new ArrayList<OutboundInterceptor<HttpServerResponse<O>>>();
        }

        private void addOut(OutboundInterceptor<HttpServerResponse<O>> interceptor) {
            interceptors.add(interceptor);
        }

        private InterceptorHolder<HttpServerRequest<I>, HttpKeyEvaluationContext, OutboundInterceptor<HttpServerResponse<O>>> buildHolder() {
            return new InterceptorHolder<HttpServerRequest<I>, HttpKeyEvaluationContext, OutboundInterceptor<HttpServerResponse<O>>>(key, interceptors);
        }
    }

    private static class HttpInClassHolder<I, O> extends
            InterceptorHolder<HttpServerRequest<I>, HttpKeyEvaluationContext,
                              Class<? extends InboundInterceptor<HttpServerRequest<I>, HttpServerResponse<O>>>> {

        private HttpInClassHolder(InterceptorKey<HttpServerRequest<I>, HttpKeyEvaluationContext> key,
                                  List<Class<? extends InboundInterceptor<HttpServerRequest<I>, HttpServerResponse<O>>>> interceptors) {
            super(key, interceptors);
        }
    }

    private static class HttpOutClassHolder<I, O> extends
            InterceptorHolder<HttpServerRequest<I>, HttpKeyEvaluationContext,
                              Class<? extends OutboundInterceptor<HttpServerResponse<O>>>> {
        private HttpOutClassHolder(InterceptorKey<HttpServerRequest<I>, HttpKeyEvaluationContext> key,
                                   List<Class<? extends OutboundInterceptor<HttpServerResponse<O>>>> interceptors) {
            super(key, interceptors);
        }
    }
}
