package com.netflix.karyon.server.http.spi;

import com.google.common.base.Preconditions;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

import javax.annotation.Nullable;

/**
 * A list of {@link AttributeKey}s that are used by the infrastructure and is free to be used by applications.
 *
 * @author Nitesh Kant
 */
public final class RequestContextAttributes {

    /**
     * In order to optimize for request URI parsing, karyon stores the {@link QueryStringDecoder} instances, if created,
     * in the {@link ChannelHandlerContext} as an {@link Attribute}. <br/>
     * This attribute is not always available and hence the users must always check for availability. It is always a
     * good practice to store the decoder back in the context, once created so that other code can use it if required.
     */
    public static final AttributeKey<QueryStringDecoder> queryDecoderKey = AttributeKey.valueOf("_queryStringDecoder");

    private RequestContextAttributes() { /// No instances are required.
    }

    /**
     * Retrieves an attribute for the passed key from the passed channel handler context & returns the value if found.
     *
     * @param attributeKey Key for which the value is to be retrieved.
     * @param context Context from which the attribute is to be retrieved. Can be {@code null} in which case, this
     *                method returns a {@code null}
     *
     * @param <T> The type of the attribute.
     *
     * @return The value of the attribute if found, {@code null} otherwise or if the context passed is {@code null}
     */
    @Nullable
    public static <T> T getAttributeValueFromContext(AttributeKey<T> attributeKey,
                                                     @Nullable ChannelHandlerContext context) {
        if (null == context) {
            return null;
        }
        return context.attr(attributeKey).get(); // Returned attribute is never null.
    }

    public static QueryStringDecoder getOrCreateQueryStringDecoder(HttpRequest request,
                                                                   ChannelHandlerContext context) {
        Preconditions.checkNotNull(context, "Channel handler context can not be null.");
        Preconditions.checkNotNull(request, "Request can not be null.");

        String uri = request.getUri();
        if (null == uri) {
            return null;
        }

        Attribute<QueryStringDecoder> queryDecoderAttr = context.attr(queryDecoderKey);

        QueryStringDecoder _queryStringDecoder = getAttributeValueFromContext(queryDecoderKey, context);

        if (null == _queryStringDecoder) {
            _queryStringDecoder = new QueryStringDecoder(uri);
            if (null != queryDecoderAttr) {
                queryDecoderAttr.setIfAbsent(_queryStringDecoder);
            }
        }
        return _queryStringDecoder;
    }
}
