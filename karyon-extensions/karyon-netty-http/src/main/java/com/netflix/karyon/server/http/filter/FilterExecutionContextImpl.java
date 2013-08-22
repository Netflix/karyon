package com.netflix.karyon.server.http.filter;

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
public class FilterExecutionContextImpl implements FilterExecutionContext {

    private final Iterator<Filter> filtersIterator;
    private final Filter tail;

    public FilterExecutionContextImpl(@NotNull List<Filter> filters, @Nullable Filter tail) {
        Preconditions.checkNotNull(filters, "Filters can not be null.");
        filtersIterator = filters.iterator();
        this.tail = tail;
    }

    @Override
    public void executeNextFilter(FullHttpRequest request, HttpResponseWriter responseWriter) {
        Filter filterToExecute = null;
        while (filtersIterator.hasNext()) {
            Filter next = filtersIterator.next();
            if (null != next) {
                filterToExecute = next;
                break;
            }
        }

        if (null == filterToExecute) {
            if (null != tail) {
                tail.filter(request, responseWriter, this);
            }
        } else {
            filterToExecute.filter(request, responseWriter, this);
        }
    }
}
