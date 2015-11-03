package com.netflix.karyon.http.simple;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import com.sun.net.httpserver.HttpExchange;

/**
 * Filter that counts number of requests per method+path combination
 */
public class PathCountingHttpHandlerFilter implements HttpHandlerFilter {
    private final ConcurrentMap<String, AtomicLong> counters;
    
    public PathCountingHttpHandlerFilter() {
        this(new ConcurrentHashMap<>());
    }
    
    public PathCountingHttpHandlerFilter(ConcurrentMap<String, AtomicLong> counters) {
        this.counters = counters;
    }
    
    @Override
    public void doFilter(HttpExchange exchange, HttpHandlerFilterChain chain) throws IOException {
        String key = exchange.getRequestMethod() + " " + exchange.getRequestURI().getPath();
        AtomicLong counter = counters.get(key);
        if (counter == null) {
            counter = new AtomicLong();
            AtomicLong existing = counters.putIfAbsent(key, counter);
            if (existing != null) 
                counter = existing;
        }
        counter.incrementAndGet();
        
        chain.doFilter(exchange);
    }

    public Map<String, AtomicLong> getCounts() {
        return Collections.unmodifiableMap(counters);
    }
}
