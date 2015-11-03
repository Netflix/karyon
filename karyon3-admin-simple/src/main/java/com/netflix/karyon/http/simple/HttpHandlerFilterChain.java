package com.netflix.karyon.http.simple;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * Encapsulate the filter chain state while processing a request.  An HttpHandlerFilterChain
 * is create when processing a request in a {@link FilteringHttpHandler}.
 */
public final class HttpHandlerFilterChain {
    private final Iterator<HttpHandlerFilter> filters;
    private final HttpHandler handler;
    
    public HttpHandlerFilterChain(List<HttpHandlerFilter> filters, HttpHandler handler) {
        this.filters = filters.iterator();
        this.handler = handler;
    }
    
    public void doFilter(HttpExchange exchange) throws IOException {
        // Call the next filter as long as there is one.  The filter is expected to recursively
        // call doFilter to invoke the next filter in the chain
        if (filters.hasNext()) {
            filters.next().doFilter(exchange, this);
        }
        // No more filters to call the actual handler
        else {
            handler.handle(exchange);
        }
    }
}
