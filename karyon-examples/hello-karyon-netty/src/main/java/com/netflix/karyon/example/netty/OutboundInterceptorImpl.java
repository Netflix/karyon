package com.netflix.karyon.example.netty;

import com.netflix.karyon.server.http.interceptor.InterceptorExecutionContext;
import com.netflix.karyon.server.http.interceptor.OutboundInterceptor;
import com.netflix.karyon.server.http.spi.HttpResponseWriter;
import io.netty.handler.codec.http.FullHttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
* @author Nitesh Kant
*/
class OutboundInterceptorImpl implements OutboundInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(OutboundInterceptorImpl.class);

    @Override
    public void interceptOut(FullHttpRequest httpRequest, HttpResponseWriter responseWriter,
                            InterceptorExecutionContext executionContext) {
        logger.info("Invoked outbound interceptor.");
        executionContext.executeNextInterceptor(httpRequest, responseWriter);
    }
}
