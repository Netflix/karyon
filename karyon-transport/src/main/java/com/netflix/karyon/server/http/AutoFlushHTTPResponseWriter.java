package com.netflix.karyon.server.http;

import com.netflix.karyon.server.spi.AutoFlushResponseWriter;
import com.netflix.karyon.server.spi.ResponseWriter;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObject;
import io.netty.util.concurrent.Future;

import static com.netflix.karyon.server.http.spi.RequestContextAttributes.getBooleanAttributeValueFromContext;
import static com.netflix.karyon.server.http.spi.RequestContextAttributes.httpKeepAliveKey;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;

/**
 * A {@link ResponseWriter} implementation for HTTP. This makes sure that the required HTTP headers are added to the
 * response which the users does not explicitly add. <br/>
 * Although not required, this will be a convenient start for any HTTP {@link ResponseWriter}
 *
 * @author Nitesh Kant
 */
public abstract class AutoFlushHTTPResponseWriter<T extends HttpObject> extends AutoFlushResponseWriter<T> {

    protected AutoFlushHTTPResponseWriter(ChannelHandlerContext context) {
        super(context);
    }

    @Override
    public Future<Void> write(T response) {

        boolean keepAlive = getBooleanAttributeValueFromContext(httpKeepAliveKey, context); // defaults to false.

        if (response.getClass().isAssignableFrom(FullHttpResponse.class)) {
            FullHttpResponse httpResponse = (FullHttpResponse) response;
            if (keepAlive) {
                httpResponse.headers().set(Names.CONTENT_LENGTH, httpResponse.content().readableBytes());
                httpResponse.headers().set(Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
            }
        }

        final Future<Void> writeFuture = super.write(response);

        if (!keepAlive) {
            writeFuture.addListener(ChannelFutureListener.CLOSE);
        }

        return writeFuture;
    }

    @Override
    public Future<Void> write(T response, ChannelPromise promise) {
        return super.write(response, promise);
    }
}
