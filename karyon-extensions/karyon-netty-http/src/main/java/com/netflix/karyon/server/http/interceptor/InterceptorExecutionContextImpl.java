package com.netflix.karyon.server.http.interceptor;

import com.google.common.base.Preconditions;
import com.netflix.karyon.server.http.spi.HttpResponseWriter;
import io.netty.handler.codec.http.FullHttpRequest;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author Nitesh Kant
 */
public class InterceptorExecutionContextImpl implements InterceptorExecutionContext {

    private final Iterator<InboundInterceptor> inboundInterceptors;
    private final InboundInterceptor inTail;

    private final Iterator<OutboundInterceptor> outboundInterceptors;
    private final OutboundInterceptor outTail;

    private final boolean isOutbound;
    private volatile boolean tailExecuted;

    public InterceptorExecutionContextImpl(List<InboundInterceptor> interceptors, @Nullable InboundInterceptor tail) {
        Preconditions.checkState(null != interceptors, "Interceptors can not be null.");
        if (!interceptors.isEmpty()) { // Optimizing empty iterator creation
            inboundInterceptors = interceptors.iterator();
        } else {
            inboundInterceptors = Collections.<InboundInterceptor>emptyList().iterator();
        }
        inTail = tail;
        outboundInterceptors = null;
        outTail = null;
        isOutbound = false;
    }

    public InterceptorExecutionContextImpl(List<OutboundInterceptor> interceptors, @Nullable OutboundInterceptor tail) {
        Preconditions.checkState(null != interceptors, "Interceptors can not be null.");
        inboundInterceptors = null;
        inTail = null;
        if (!interceptors.isEmpty()) { // Optimizing empty iterator creation
            outboundInterceptors = interceptors.iterator();
        } else {
            outboundInterceptors = Collections.<OutboundInterceptor>emptyList().iterator();
        }
        outTail = tail;
        isOutbound = true;
    }

    @Override
    public void executeNextInterceptor(FullHttpRequest request, HttpResponseWriter responseWriter) {
        if (isOutbound) {
            executeOut(request, responseWriter);
        } else {
            executeIn(request, responseWriter);
        }
    }

    private void executeIn(FullHttpRequest request, HttpResponseWriter responseWriter) {
        InboundInterceptor nextInterceptor = null;
        while (inboundInterceptors.hasNext()) {
            InboundInterceptor next = inboundInterceptors.next();
            if (null != next) {
                nextInterceptor = next;
                break;
            }
        }

        if (null == nextInterceptor) {
            if (null != inTail && !tailExecuted) {
                tailExecuted = true; // Even if tail invocation failed, we should set this flag, so we don't invoke it again.
                inTail.interceptIn(request, responseWriter, this);
            }
        } else {
            nextInterceptor.interceptIn(request, responseWriter, this);
        }
    }

    private void executeOut(FullHttpRequest request, HttpResponseWriter responseWriter) {
        OutboundInterceptor nextInterceptor = null;
        while (outboundInterceptors.hasNext()) {
            OutboundInterceptor next = outboundInterceptors.next();
            if (null != next) {
                nextInterceptor = next;
                break;
            }
        }

        if (null == nextInterceptor) {
            if (null != outTail && !tailExecuted) {
                tailExecuted = true; // Even if tail invocation failed, we should set this flag, so we don't invoke it again.
                outTail.interceptOut(request, responseWriter, this);
            }
        } else {
            nextInterceptor.interceptOut(request, responseWriter, this);
        }
    }
}
