package com.netflix.karyon.server.http.interceptor;

import com.google.common.collect.Multimap;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;

import javax.annotation.Nullable;

/**
 * A definition of all interceptors that are confiured for any application. <br/>
 * At runtime an instance of {@link PipelineFactory} will execute all {@link Key}s returned from
 * {@link #getInboundInterceptors()} and {@link #getOutboundInterceptors()} to determine which interceptors are to be
 * applied for a particular request.
 *
 * @author Nitesh Kant
 */
public interface PipelineDefinition {

    /**
     * Returns a super set of all interceptors defined for the application. <br/>
     * {@link Key#apply(FullHttpRequest, Key.KeyEvaluationContext)} will be invoked on the returned keys to determine which interceptors apply for a
     * particular request.
     *
     * @return A super set of all interceptors defined for the application.
     */
    Multimap<Key, InboundInterceptor> getInboundInterceptors();

    /**
     * Returns a super set of all interceptors defined for the application. <br/>
     * {@link Key#apply(FullHttpRequest, Key.KeyEvaluationContext)} will be invoked on the returned keys to determine which interceptors apply for a
     * particular request.
     *
     * @return A super set of all interceptors defined for the application.
     */
    Multimap<Key, OutboundInterceptor> getOutboundInterceptors();

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
        boolean apply(FullHttpRequest request, KeyEvaluationContext context);

        /**
         * A context to store results of costly operations during evaluation of filter keys, eg: request URI parsing. <p/>
         * <b>This context is not thread-safe.</b>
         */
        class KeyEvaluationContext {

            @Nullable
            private volatile QueryStringDecoder queryStringDecoder;

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

                String uri = httpRequest.getUri();
                if (null == uri) {
                    return null;
                }

                if (null == queryStringDecoder) {
                    if (!uri.endsWith("/") && !uri.contains(".") && !uri.contains("?")) {
                        // Normalize the URI for better matching of Servlet style URI constraints.
                        uri += "/";
                    }
                    queryStringDecoder = new QueryStringDecoder(uri);
                }

                return queryStringDecoder.path();
            }
        }
    }
}
