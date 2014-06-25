package com.netflix.karyon.server.http.interceptor;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.Multimap;
import com.netflix.karyon.server.http.spi.QueryStringDecoder;
import com.netflix.karyon.server.http.spi.RequestContextAttributes;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;

import javax.annotation.Nullable;

/**
 * A definition of all interceptors that are confiured for any application. <br/>
 * At runtime an instance of {@link PipelineFactory} will execute all {@link Key}s returned from
 * {@link #getInboundInterceptors()} and {@link #getOutboundInterceptors()} to determine which interceptors are to be
 * applied for a particular request.
 *
 * @author Nitesh Kant
 */
public interface PipelineDefinition<I extends HttpObject, O extends HttpObject> {

    /**
     * Returns a super set of all interceptors defined for the application. <br/>
     * {@link Key#apply(HttpRequest, Key.KeyEvaluationContext)} will be invoked on the returned keys to determine which interceptors apply for a
     * particular request.
     *
     * @return A super set of all interceptors defined for the application.
     */
    Multimap<Key, InboundInterceptor<I, O>> getInboundInterceptors();

    /**
     * Returns a super set of all interceptors defined for the application. <br/>
     * {@link Key#apply(HttpRequest, Key.KeyEvaluationContext)} will be invoked on the returned keys
     * to determine which interceptors apply for a particular request.
     *
     * @return A super set of all interceptors defined for the application.
     */
    Multimap<Key, OutboundInterceptor<O>> getOutboundInterceptors();

    /**
     * Key for a {@link InboundInterceptor} or {@link OutboundInterceptor} which determines whether a interceptor must
     * be applied for a particular request. <br/>
     * Any implementation for the key must be aware that it will be invoked per request so it should always optimize
     * for speed of evaluation.
     */
    interface Key {

        /**
         * This is invoked for a request to determine whether the interceptor attached to this key must be executed for this
         * request.
         *
         *
         * @param request Request to determine whether the attached interceptor is to be applied or not.
         * @param context Context for the key evaluation, usually used to cache costly operations like parsing request
         *                URI.
         *
         * @return {@code true} if the interceptor must be applied for this request.
         */
        boolean apply(HttpRequest request, KeyEvaluationContext context);

        /**
         * A context to store results of costly operations during evaluation of filter keys, eg: request URI parsing. <p/>
         * <b>This context is not thread-safe.</b>
         */
        class KeyEvaluationContext {

            @Nullable private final ChannelHandlerContext channelHandlerContext;
            @Nullable private QueryStringDecoder queryStringDecoder;

            @VisibleForTesting
            KeyEvaluationContext() {
                this(null);
            }

            public KeyEvaluationContext(@Nullable ChannelHandlerContext channelHandlerContext) {
                this.channelHandlerContext = channelHandlerContext;
            }

            /**
             * Parses (if not done previously) and returns the path component in the URI.
             *
             * @param httpRequest HTTP request for which the URI path is to be returned.
             *
             * @return The path component of the URI (as returned by {@link HttpRequest#getUri()} or {@code null} if the
             * URI is null.
             */
            @Nullable
            String getRequestUriPath(HttpRequest httpRequest) {
                Preconditions.checkNotNull(httpRequest, "Http request can not be null.");
                String uri = httpRequest.getUri();
                if (null == uri) {
                    return null;
                }

                if (null == queryStringDecoder) {
                    if (null == channelHandlerContext) {
                        queryStringDecoder = new QueryStringDecoder(uri);
                    } else {
                        queryStringDecoder = RequestContextAttributes.getOrCreateQueryStringDecoder(httpRequest,
                                                                                                    channelHandlerContext);
                    }
                }

                return queryStringDecoder.nettyDecoder().path();
            }
        }
    }
}
