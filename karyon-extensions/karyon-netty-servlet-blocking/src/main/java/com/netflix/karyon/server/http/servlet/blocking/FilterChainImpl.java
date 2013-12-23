package com.netflix.karyon.server.http.servlet.blocking;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;

import javax.annotation.Nullable;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * Implementation of {@link FilterChain}
 *
 * @author Nitesh Kant
 */
public class FilterChainImpl implements FilterChain {

    private final HttpServlet servlet;
    private final Iterator<? extends Filter> filtersIterator;
    private boolean servletInvoked;

    public FilterChainImpl(HttpServlet servlet, @Nullable List<? extends Filter> filters) {
        Preconditions.checkNotNull(servlet, "Servlet can not be null.");
        this.servlet = servlet;
        filtersIterator = null == filters ? Iterators.<Filter>emptyIterator() : filters.iterator();
    }

    @Override
    public synchronized void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
        if (filtersIterator.hasNext()) {
            Filter nextFilter = filtersIterator.next();
            nextFilter.doFilter(request, response, this);
        } else if(!servletInvoked) {
            servletInvoked = true;
            servlet.service(request, response);
        }
    }
}
