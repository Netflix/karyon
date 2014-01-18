package com.netflix.karyon.example.netty;

import com.netflix.karyon.server.http.interceptor.InboundInterceptor;
import com.netflix.karyon.server.http.interceptor.NextInterceptorInvoker;
import com.netflix.karyon.server.http.interceptor.OutboundInterceptor;
import com.netflix.karyon.server.spi.ResponseWriter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
* @author Nitesh Kant
*/
class LoggingInterceptor implements InboundInterceptor<FullHttpRequest, FullHttpResponse>, OutboundInterceptor<FullHttpResponse> {

    private static final Logger logger = LoggerFactory.getLogger(LoggingInterceptor.class);

    private static int count;
    private final int id;

    LoggingInterceptor() {
        id = ++count;
    }

    @Override
    public void interceptIn(FullHttpRequest httpRequest, ResponseWriter<FullHttpResponse> responseWriter,
                            NextInterceptorInvoker<FullHttpRequest, FullHttpResponse> invoker) {
        logger.info("Logging interceptor with id {} invoked for direction IN.", id);
        invoker.executeNext(httpRequest, responseWriter);
    }

    @Override
    public void interceptOut(FullHttpResponse httpResponse, ResponseWriter<FullHttpResponse> responseWriter,
                             NextInterceptorInvoker<FullHttpResponse, FullHttpResponse> invoker) {
        logger.info("Logging interceptor with id {} invoked for direction OUT.", id);
        invoker.executeNext(httpResponse, responseWriter);
    }
}
