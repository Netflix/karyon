package com.netflix.karyon.example.netty;

import com.netflix.karyon.server.http.interceptor.BidirectionalInterceptorAdapter;
import com.netflix.karyon.server.http.interceptor.InterceptorExecutionContext;
import com.netflix.karyon.server.http.spi.HttpResponseWriter;
import io.netty.handler.codec.http.FullHttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
* @author Nitesh Kant
*/
class LoggingInterceptor extends BidirectionalInterceptorAdapter {

    private static final Logger logger = LoggerFactory.getLogger(LoggingInterceptor.class);

    private static int count;
    private final int id;

    LoggingInterceptor() {
        id = ++count;
    }

    @Override
    protected void intercept(Direction direction, FullHttpRequest httpRequest,
                             HttpResponseWriter responseWriter, InterceptorExecutionContext executionContext) {
        logger.info("Logging interceptor with id {} invoked for direction {}.", id, direction.name());
        executionContext.executeNextInterceptor(httpRequest, responseWriter);
    }
}
