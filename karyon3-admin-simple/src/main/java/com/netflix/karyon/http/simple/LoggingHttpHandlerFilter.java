package com.netflix.karyon.http.simple;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.net.httpserver.HttpExchange;

/**
 * Filter that logs all request start and end.  The log message include timing as well as error 
 * information.
 */
public class LoggingHttpHandlerFilter implements HttpHandlerFilter {
    private static final Logger LOG = LoggerFactory.getLogger(LoggingHttpHandlerFilter.class);
    
    @Override
    public void doFilter(HttpExchange exchange, HttpHandlerFilterChain chain) throws IOException {
        
        long startTime = System.nanoTime();
        Exception error = null;
        try {
            LOG.debug("Starting {} {}", exchange.getRequestMethod(), exchange.getRequestURI());
            chain.doFilter(exchange);
        }
        catch (IOException e) {
            error = e;
            throw e;
        }
        catch (Exception e) {
            error = e;
            throw new IOException("Error processing request", e);
        }
        finally {
            long endTime = System.nanoTime();
            LOG.debug("Ending {} {} ({} msec)", 
                    exchange.getRequestMethod(), 
                    exchange.getRequestURI(), 
                    TimeUnit.MILLISECONDS.convert(endTime-startTime, TimeUnit.NANOSECONDS),
                    error);
        }
    }

}
