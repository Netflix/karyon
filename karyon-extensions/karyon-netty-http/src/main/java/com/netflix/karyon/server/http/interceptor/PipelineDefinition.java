package com.netflix.karyon.server.http.interceptor;

import com.google.common.collect.Multimap;
import io.netty.handler.codec.http.FullHttpRequest;

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
     * {@link Key#apply(FullHttpRequest)} will be invoked on the returned keys to determine which interceptors apply for a
     * particular request.
     *
     * @return A super set of all interceptors defined for the application.
     */
    Multimap<Key, InboundInterceptor> getInboundInterceptors();

    /**
     * Returns a super set of all interceptors defined for the application. <br/>
     * {@link Key#apply(FullHttpRequest)} will be invoked on the returned keys to determine which interceptors apply for a
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
         * @param request Request to determine whether the attached interceptor is to be applied or not.
         *
         * @return {@code true} if the interceptor must be applied for this request.
         */
        boolean apply(FullHttpRequest request);

    }
}
