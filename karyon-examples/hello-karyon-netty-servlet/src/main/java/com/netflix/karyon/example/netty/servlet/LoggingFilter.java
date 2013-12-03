package com.netflix.karyon.example.netty.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

/**
 * @author Nitesh Kant
 */
public class LoggingFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // TODO: Auto-generated method stub

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        logger.info("Received a request on the filter.");
        chain.doFilter(request, response);

    }

    @Override
    public void destroy() {
        // TODO: Auto-generated method stub

    }
}
