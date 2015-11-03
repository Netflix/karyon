package com.netflix.karyon.http.simple;

import java.io.IOException;
import java.util.List;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * HttpHandler with support for per request filters
 */
public class FilteringHttpHandler implements HttpHandler {
    private final List<HttpHandlerFilter> filters;
    private final HttpHandler handler;
    
    public FilteringHttpHandler(List<HttpHandlerFilter> filters, HttpHandler handler) {
        this.filters = filters;
        this.handler = handler;
    }
    
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        new HttpHandlerFilterChain(filters, handler).doFilter(exchange);
    }
}
