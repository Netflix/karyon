package com.netflix.karyon.example.netty;

import com.netflix.karyon.server.http.interceptor.InboundInterceptor;
import com.netflix.karyon.server.http.interceptor.NextInterceptorInvoker;
import com.netflix.karyon.server.spi.ResponseWriter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
* @author Nitesh Kant
*/
class InboundInterceptorImpl implements InboundInterceptor<FullHttpRequest, FullHttpResponse> {

    private static final Logger logger = LoggerFactory.getLogger(InboundInterceptorImpl.class);

    @Override
    public void interceptIn(FullHttpRequest httpRequest, ResponseWriter<FullHttpResponse> responseWriter,
                            NextInterceptorInvoker<FullHttpRequest, FullHttpResponse> executionContext) {
        logger.info("Invoked inbound interceptor.");
        executionContext.executeNext(httpRequest, responseWriter);
    }
}
