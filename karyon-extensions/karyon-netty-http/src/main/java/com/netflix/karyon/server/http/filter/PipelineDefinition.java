package com.netflix.karyon.server.http.filter;

import io.netty.handler.codec.http.FullHttpRequest;

import java.util.Map;

/**
 * A definition of all filters that are confiured for any application. <br/>
 * At runtime an instance of {@link PipelineFactory} will execute all {@link Key}s returned from {@link #getFilters()}
 * to determine which filters will be applied for a particular request.
 *
 * @author Nitesh Kant
 */
public interface PipelineDefinition {

    /**
     * Retruns a super set of all filters defined for the application. <br/>
     * {@link Key#apply(FullHttpRequest)} will be invoked on the returned keys to determine which filters apply for a
     * particular request.
     *
     * @return A super set of all filters defined for the application.
     */
    Map<Key, Filter> getFilters();

    /**
     * Key for a {@link Filter} which determines whether a filter must be applied for a particular request. <br/>
     * Any implementation for the key must be aware that it will be invoked per request so it should always optimize
     * for speed of evaluation.
     */
    interface Key {

        /**
         * This is invoked for a request to determine whether the filter attached to this key must be executed for this
         * request.
         *
         * @param request Request to determine whether the attached filter is to be applied or not.
         *
         * @return {@code true} if the filter must be applied for this request.
         */
        boolean apply(FullHttpRequest request);

    }
}
