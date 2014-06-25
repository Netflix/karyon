package com.netflix.karyon.server.http;

import com.netflix.karyon.server.http.spi.HttpRequestRouter;
import com.netflix.karyon.server.spi.ResponseWriter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.SucceededFuture;

/**
 * @author Nitesh Kant
 */
public class TestableRequestRouter implements HttpRequestRouter<FullHttpRequest, FullHttpResponse> {

    public static final HttpResponseStatus ROUTER_RESPONSE_STATUS_DEFAULT = HttpResponseStatus.NO_CONTENT;
    private volatile boolean executed;

    @Override
    public Future<Void> process(FullHttpRequest request, ResponseWriter<FullHttpResponse> responseWriter) {
        // enqueue to outbound
        executed = true;
        responseWriter.write(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, ROUTER_RESPONSE_STATUS_DEFAULT));
        return new SucceededFuture<Void>(null, null);
    }

    public boolean isExecuted() {
        return executed;
    }
}
