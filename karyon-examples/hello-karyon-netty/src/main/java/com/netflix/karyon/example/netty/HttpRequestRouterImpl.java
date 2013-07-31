package com.netflix.karyon.example.netty;

import com.netflix.karyon.server.netty.spi.HttpRequestRouter;
import com.netflix.karyon.server.netty.spi.HttpResponseWriter;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.QueryStringDecoder;

import java.util.Map;

/**
 * @author Nitesh Kant
 */
public class HttpRequestRouterImpl implements HttpRequestRouter {

    private final Map<String, String> pathVsContents;

    public HttpRequestRouterImpl(Map<String, String> pathVsContent) {
        pathVsContents = pathVsContent;
    }

    @Override
    public void process(FullHttpRequest request, HttpResponseWriter responseWriter) {
        QueryStringDecoder qpDecoder = new QueryStringDecoder(request.getUri());
        String path = qpDecoder.path();
        String content = pathVsContents.get(path);
        if (null != content) {
            ByteBuf contentBuffer = responseWriter.getChannelHandlerContext().channel().alloc().buffer(content.length());
            contentBuffer.writeBytes(content.getBytes());
            responseWriter.createResponse(HttpResponseStatus.OK, contentBuffer);
        } else {
            responseWriter.createResponse(HttpResponseStatus.NOT_FOUND, null);
        }
        responseWriter.sendResponse();
    }

    @Override
    public boolean isBlocking() {
        return false;
    }
}
