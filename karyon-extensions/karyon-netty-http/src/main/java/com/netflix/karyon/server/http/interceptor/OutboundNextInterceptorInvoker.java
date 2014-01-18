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
public class OutboundNextInterceptorInvoker<T extends HttpObject> implements NextInterceptorInvoker<T, T> {

    private final Iterator<OutboundInterceptor<T>> outboundInterceptors;
    private final OutboundInterceptor<T> outTail;
    private volatile boolean tailExecuted;

    public OutboundNextInterceptorInvoker(List<OutboundInterceptor<T>> interceptors,
                                          @Nullable OutboundInterceptor<T> tail) {
        Preconditions.checkNotNull(interceptors, "Interceptors can not be null.");
        if (!interceptors.isEmpty()) { // Optimizing empty iterator creation
            outboundInterceptors = interceptors.iterator();
        } else {
            outboundInterceptors = Collections.<OutboundInterceptor<T>>emptyList().iterator();
        }
        outTail = tail;
    }

    @Override
    public void executeNext(T httpMessage, ResponseWriter<T> responseWriter) {
        OutboundInterceptor<T> nextInterceptor = null;
        while (outboundInterceptors.hasNext()) {
            OutboundInterceptor<T> next = outboundInterceptors.next();
            if (null != next) {
                nextInterceptor = next;
                break;
            }
        }

        if (null == nextInterceptor) {
            if (null != outTail && !tailExecuted) {
                tailExecuted = true; // Even if tail invocation failed, we should set this flag, so we don't invoke it again.
                outTail.interceptOut(httpMessage, responseWriter, this);
            }
        } else {
            nextInterceptor.interceptOut(httpMessage, responseWriter, this);
        }
    }
}
