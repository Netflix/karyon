package com.netflix.karyon.server.http.interceptor;

import com.google.common.base.Preconditions;
import com.netflix.karyon.server.http.spi.HttpResponseWriter;
import io.netty.handler.codec.http.FullHttpRequest;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.Iterator;
import java.util.List;

/**
 * @author Nitesh Kant
 */
public class InterceptorExecutionContextImpl implements InterceptorExecutionContext {

    private final Iterator<Interceptor> filtersIterator;
    private final Interceptor tail;

    public InterceptorExecutionContextImpl(@NotNull List<Interceptor> interceptors, @Nullable Interceptor tail) {
        Preconditions.checkNotNull(interceptors, "Filters can not be null.");
        filtersIterator = interceptors.iterator();
        this.tail = tail;
    }

    @Override
    public void executeNextInterceptor(FullHttpRequest request, HttpResponseWriter responseWriter) {
        Interceptor interceptorsToExecute = null;
        while (filtersIterator.hasNext()) {
            Interceptor next = filtersIterator.next();
            if (null != next) {
                interceptorsToExecute = next;
                break;
            }
        }

        if (null == interceptorsToExecute) {
            if (null != tail) {
                tail.filter(request, responseWriter, this);
            }
        } else {
            interceptorsToExecute.filter(request, responseWriter, this);
        }
    }
}
