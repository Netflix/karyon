package netflix.karyon.transport.http;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import netflix.karyon.transport.interceptor.KeyEvaluationContext;

/**
 * @author Nitesh Kant
 */
public class HttpKeyEvaluationContext extends KeyEvaluationContext {

    /**
     * In order to optimize for request URI parsing, karyon stores the {@link QueryStringDecoder} instances, if created,
     * in the {@link io.netty.channel.ChannelHandlerContext} as an {@link io.netty.util.Attribute}. <br>
     * This attribute is not always available and hence the users must always check for availability. It is always a
     * good practice to store the decoder back in the context, once created so that other code can use it if required.
     */
    public static final AttributeKey<QueryStringDecoder> queryDecoderKey = AttributeKey.valueOf("_queryStringDecoder");

    private QueryStringDecoder queryStringDecoder;

    public HttpKeyEvaluationContext(Channel channel) {
        super(channel);
        channel.attr(queryDecoderKey).remove();
    }

    /**
     * Parses (if not done previously) and returns the path component in the URI.
     *
     * @param httpRequest HTTP request for which the URI path is to be returned.
     *
     * @return The path component of the URI (as returned by {@link HttpRequest#getUri()} or {@code null} if the
     * URI is null.
     */
    String getRequestUriPath(HttpServerRequest<?> httpRequest) {
        String uri = httpRequest.getUri();
        if (null == uri) {
            return null;
        }

        if (null == queryStringDecoder) {
            if (null == channel) {
                queryStringDecoder = new QueryStringDecoder(uri);
            } else {
                queryStringDecoder = getOrCreateQueryStringDecoder(httpRequest);
            }
        }

        return queryStringDecoder.nettyDecoder().path();
    }

    private QueryStringDecoder getOrCreateQueryStringDecoder(HttpServerRequest<?> request) {
        if (null == request) {
            throw new NullPointerException("Request can not be null.");
        }
        String uri = request.getUri();
        if (null == uri) {
            return null;
        }

        Attribute<QueryStringDecoder> queryDecoderAttr = channel.attr(queryDecoderKey);

        QueryStringDecoder _queryStringDecoder = queryDecoderAttr.get();

        if (null == _queryStringDecoder) {
            _queryStringDecoder = new QueryStringDecoder(uri);
            queryDecoderAttr.setIfAbsent(_queryStringDecoder);
        }
        return _queryStringDecoder;
    }

    public static QueryStringDecoder getOrCreateQueryStringDecoder(HttpServerRequest<?> request,
                                                                   ChannelHandlerContext channelHandlerContext) {
        if (null == request) {
            throw new NullPointerException("Request can not be null.");
        }

        String uri = request.getUri();
        if (null == uri) {
            return null;
        }

        Attribute<QueryStringDecoder> queryDecoderAttr = channelHandlerContext.attr(queryDecoderKey);

        QueryStringDecoder _queryStringDecoder = queryDecoderAttr.get();

        if (null == _queryStringDecoder) {
            _queryStringDecoder = new QueryStringDecoder(uri);
            queryDecoderAttr.setIfAbsent(_queryStringDecoder);
        }
        return _queryStringDecoder;
    }

}
