package com.netflix.karyon.server.http.servlet.blocking;

import javax.annotation.Nullable;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
* @author Nitesh Kant
*/
class TestableFilter implements Filter {

    public HttpServletRequest req;
    public HttpServletResponse resp;
    public int indexInChain;
    public int invokedOrder;
    public volatile boolean invoked;
    private final AtomicInteger invocationOrderCounter;
    private final boolean throwException;

    TestableFilter(int indexInChain, @Nullable AtomicInteger invocationOrderCounter, boolean throwException) {
        this.indexInChain = indexInChain;
        this.invocationOrderCounter = invocationOrderCounter;
        this.throwException = throwException;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        req = (HttpServletRequest) request;
        resp = (HttpServletResponse) response;
        invoked = true;
        invokedOrder = null != invocationOrderCounter ? invocationOrderCounter.incrementAndGet() : 0;
        if (throwException) {
            throw new IllegalStateException("Explicit exception throwing.");
        } else {
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {
    }
}
