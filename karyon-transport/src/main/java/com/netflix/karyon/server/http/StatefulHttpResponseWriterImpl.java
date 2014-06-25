package com.netflix.karyon.server.http;

import com.google.common.base.Preconditions;
import com.netflix.karyon.server.http.spi.StatefulHttpResponseWriter;
import com.netflix.karyon.server.spi.ResponseWriterFactory;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

import static com.netflix.karyon.server.http.spi.RequestContextAttributes.getAttributeValueFromContext;
import static com.netflix.karyon.server.http.spi.RequestContextAttributes.httpProtocolVersionKey;

/**
 * An implementation of {@link StatefulHttpResponseWriter}. <br/>
 * This implementation auto-flushes the response. <br/>
 * You can use {@link ResponseWriterFactoryImpl} as the {@link ResponseWriterFactory} implementation for creating this
 * class. <br/>
 *
 * @author Nitesh Kant
 */
public class StatefulHttpResponseWriterImpl extends AutoFlushHTTPResponseWriter<FullHttpResponse>
        implements StatefulHttpResponseWriter {

    private static final Logger logger = LoggerFactory.getLogger(StatefulHttpResponseWriterImpl.class);

    private enum ResponseState { NotCreated, Created, Sent}

    private volatile DefaultFullHttpResponse response;
    private volatile ResponseState responseState = ResponseState.NotCreated;

    public StatefulHttpResponseWriterImpl(ChannelHandlerContext ctx) {
        super(ctx);
    }

    @Override
    public FullHttpResponse createResponse(HttpResponseStatus responseStatus, @Nullable ByteBuf content) {
        Preconditions.checkState(!isResponseCreated(), "Response already created.");

        if (null == content) {
            content = Unpooled.buffer(0);
        }
        HttpVersion protocolVersion = getAttributeValueFromContext(httpProtocolVersionKey, context);
        if (null == protocolVersion) {
            protocolVersion = HttpVersion.HTTP_1_1;
        }
        response = new DefaultFullHttpResponse(protocolVersion, responseStatus, content);
        responseState = ResponseState.Created;
        return response;
    }

    @Override
    public void sendResponse() {
        validateReadyForSend();
        if (responseState == ResponseState.Sent) {
            if (logger.isInfoEnabled()) {
                logger.info("Response already sent, ignoring this sendResponse call. Dumping stacktrace as exception for debugging.",
                            new IllegalStateException());
            }
            return;
        }

        write(response);

        responseState = ResponseState.Sent;
    }

    void validateReadyForSend() {
        Preconditions.checkState(null != response,
                                 "No response instance created. You must create a response before send.");
    }

    @Nullable
    @Override
    public FullHttpResponse response() {
        return response;
    }

    @Override
    public boolean isResponseCreated() {
        return ResponseState.NotCreated != responseState;
    }

    @Override
    public boolean isResponseSent() {
        return ResponseState.Sent == responseState;
    }

    DefaultFullHttpResponse getResponse() {
        return response;
    }

    public static class ResponseWriterFactoryImpl
            implements ResponseWriterFactory<FullHttpResponse> {

        @Override
        public StatefulHttpResponseWriter newWriter(ChannelHandlerContext channelHandlerContext) {
            return new StatefulHttpResponseWriterImpl(channelHandlerContext);
        }
    }
}
