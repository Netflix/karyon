package com.netflix.karyon.server.http;

import com.netflix.karyon.server.http.spi.HttpRequestRouter;
import com.netflix.karyon.server.http.spi.HttpResponseWriter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * @author Nitesh Kant
 */
public class TestableRequestRouter implements HttpRequestRouter {

    public static final HttpResponseStatus ROUTER_RESPONSE_STATUS_DEFAULT = HttpResponseStatus.NO_CONTENT;
    private volatile boolean executed;

    @Override
    public boolean isBlocking() {
        return false;
    }

    @Override
    public void process(FullHttpRequest request, HttpResponseWriter responseWriter) {
        executed = true;
        if (!responseWriter.isResponseCreated()) {
            responseWriter.createResponse(ROUTER_RESPONSE_STATUS_DEFAULT, null);
        }
        responseWriter.sendResponse();
    }

    @Override
    public void start() {
    }

    public boolean isExecuted() {
        return executed;
    }
}
