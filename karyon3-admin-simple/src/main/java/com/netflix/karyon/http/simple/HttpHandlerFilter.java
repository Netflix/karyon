package com.netflix.karyon.http.simple;

import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;

/**
 * Simple filter for an HttpExchange.  Filters are added as a list of the constructor of 
 * SimpleHttpServer.
 * 
 */
public interface HttpHandlerFilter {
    /**
     * Perform the filter on the provided HttpExchange request.  Call chain#doFilter to
     * invoke the next filter (or the handler) in the chain.
     * 
     * @param exchange  The exchange instance for this request
     * @param chain  Filter chain to call when propagating the request 
     */
    void doFilter(HttpExchange exchange, HttpHandlerFilterChain chain) throws IOException;
}
