package com.netflix.karyon.example.netty;

import com.netflix.karyon.server.http.interceptor.InboundInterceptor;
import com.netflix.karyon.server.http.interceptor.InterceptorExecutionContext;
import com.netflix.karyon.server.http.spi.HttpResponseWriter;
import io.netty.handler.codec.http.FullHttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
* @author Nitesh Kant
*/
class InboundInterceptorImpl implements InboundInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(InboundInterceptorImpl.class);

    @Override
    public void interceptIn(FullHttpRequest httpRequest, HttpResponseWriter responseWriter,
                            InterceptorExecutionContext executionContext) {
        logger.info("Invoked inbound interceptor.");
        executionContext.executeNextInterceptor(httpRequest, responseWriter);
    }
}
