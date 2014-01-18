package com.netflix.karyon.example.netty;

import com.netflix.karyon.server.http.interceptor.NextInterceptorInvoker;
import com.netflix.karyon.server.http.interceptor.OutboundInterceptor;
import com.netflix.karyon.server.spi.ResponseWriter;
import io.netty.handler.codec.http.FullHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
* @author Nitesh Kant
*/
class OutboundInterceptorImpl implements OutboundInterceptor<FullHttpResponse> {

    private static final Logger logger = LoggerFactory.getLogger(OutboundInterceptorImpl.class);

    @Override
    public void interceptOut(FullHttpResponse httpResponse, ResponseWriter<FullHttpResponse> responseWriter,
                             NextInterceptorInvoker<FullHttpResponse, FullHttpResponse> invoker) {
        logger.info("Invoked outbound interceptor.");
        invoker.executeNext(httpResponse, responseWriter);
    }
}
