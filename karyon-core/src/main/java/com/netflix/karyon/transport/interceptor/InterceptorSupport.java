package com.netflix.karyon.transport.interceptor;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A contract to support interceptors for requests i.e. how to attach filters to a request processing.
 *
 * @author Nitesh Kant
 */
public class InterceptorSupport<I, O, C extends KeyEvaluationContext> {

    private final Map<InterceptorKey<I, C>, Attacher> attachers;
    private List<InterceptorHolder<InboundInterceptor<I, O>>> inboundInterceptors;
    private List<InterceptorHolder<OutboundInterceptor<O>>> outboundInterceptors;
    private boolean finished;

    public InterceptorSupport() {
        inboundInterceptors = new LinkedList<InterceptorHolder<InboundInterceptor<I, O>>>();
        outboundInterceptors = new LinkedList<InterceptorHolder<OutboundInterceptor<O>>>();
        attachers = new HashMap<InterceptorKey<I, C>, Attacher>();
    }

    /*Package private as the returned object is mutable*/ List<InterceptorHolder<InboundInterceptor<I, O>>> getInboundInterceptors() {
        return finished ? inboundInterceptors : Collections.unmodifiableList(inboundInterceptors);
    }

    /*Package private as the returned object is mutable*/ List<InterceptorHolder<OutboundInterceptor<O>>> getOutboundInterceptors() {
        return finished ? outboundInterceptors : Collections.unmodifiableList(outboundInterceptors);
    }

    public Attacher forKey(InterceptorKey<I, C> key) {
        if (finished) {
            throw new IllegalArgumentException("Interceptor support can not be modified after finishing.");
        }
        Attacher attacher = attachers.get(key);
        if (null == attacher) {
            attacher = new Attacher(key);
            attachers.put(key, attacher);
        }
        return attacher;
    }

    public boolean hasAtleastOneInterceptor() {
        return !inboundInterceptors.isEmpty() || !outboundInterceptors.isEmpty();
    }

    public InterceptorSupport<I, O, C> finish() {
        if (!finished) {
            _finish();
            finished = true;
        }
        return this;
    }

    protected void _finish() {
        attachers.clear();
        inboundInterceptors = Collections.unmodifiableList(inboundInterceptors);
        outboundInterceptors = Collections.unmodifiableList(outboundInterceptors);
    }

    public class Attacher {

        private final InterceptorKey<I, C> key;

        public Attacher(InterceptorKey<I, C> key) {
            this.key = key;
        }

        public InterceptorSupport<I, O, C> intercept(InboundInterceptor<I, O>... interceptors) {
            inboundInterceptors.add(new InterceptorHolder<InboundInterceptor<I, O>>(key, interceptors));
            return InterceptorSupport.this;
        }

        public InterceptorSupport<I, O, C> intercept(OutboundInterceptor<O>... interceptors) {
            outboundInterceptors.add(new InterceptorHolder<OutboundInterceptor<O>>(key, interceptors));
            return InterceptorSupport.this;
        }

        public InterceptorSupport<I, O, C> intercept(DuplexInterceptor<I, O>... interceptors) {
            inboundInterceptors.add(new InterceptorHolder<InboundInterceptor<I, O>>(key, interceptors));
            outboundInterceptors.add(new InterceptorHolder<OutboundInterceptor<O>>(key, interceptors));
            return InterceptorSupport.this;
        }

        public InterceptorKey<I, C> getKey() {
            return key;
        }
    }

    /*Package private as it is mutable*/protected class InterceptorHolder<T> {

        private final InterceptorKey<I, C> key;
        private final T[] interceptors;

        public InterceptorHolder(InterceptorKey<I, C> key, T[] interceptors) {
            this.key = key;
            this.interceptors = interceptors;
        }

        public InterceptorKey<I, C> getKey() {
            return key;
        }

        public T[] getInterceptors() {
            return interceptors;
        }
    }
}
