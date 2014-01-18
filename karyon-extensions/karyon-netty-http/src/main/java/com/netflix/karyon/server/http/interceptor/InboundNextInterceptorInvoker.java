package com.netflix.karyon.server.http.interceptor;

import com.google.common.base.Preconditions;
import com.netflix.karyon.server.spi.ResponseWriter;
import io.netty.handler.codec.http.HttpObject;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author Nitesh Kant
 */
public class InboundNextInterceptorInvoker<T extends HttpObject, O extends HttpObject> implements NextInterceptorInvoker<T, O> {

    private final Iterator<InboundInterceptor<T, O>> inboundInterceptors;
    private final InboundInterceptor<T, O> inTail;
    private volatile boolean tailExecuted;

    public InboundNextInterceptorInvoker(List<InboundInterceptor<T, O>> interceptors,
                                         @Nullable InboundInterceptor<T, O> tail) {
        Preconditions.checkNotNull(interceptors, "Interceptors can not be null.");
        if (!interceptors.isEmpty()) { // Optimizing empty iterator creation
            inboundInterceptors = interceptors.iterator();
        } else {
            inboundInterceptors = Collections.<InboundInterceptor<T, O>>emptyList().iterator();
        }
        inTail = tail;
    }

    @Override
    public void executeNext(T httpMessage, ResponseWriter<O> responseWriter) {
        InboundInterceptor<T, O> nextInterceptor = null;
        while (inboundInterceptors.hasNext()) {
            InboundInterceptor<T, O> next = inboundInterceptors.next();
            if (null != next) {
                nextInterceptor = next;
                break;
            }
        }

        if (null == nextInterceptor) {
            if (null != inTail && !tailExecuted) {
                tailExecuted = true; // Even if tail invocation failed, we should set this flag, so we don't invoke it again.
                inTail.interceptIn(httpMessage, responseWriter, this);
            }
        } else {
            nextInterceptor.interceptIn(httpMessage, responseWriter, this);
        }
    }
}
