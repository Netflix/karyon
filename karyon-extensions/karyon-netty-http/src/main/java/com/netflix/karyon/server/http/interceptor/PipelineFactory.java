package com.netflix.karyon.server.http.interceptor;

import io.netty.handler.codec.http.FullHttpRequest;

import java.util.List;

/**
 * A factory that is invoked at the time of request execution to get the list of interceptors for a particular request. <br/>
 * An implementation will typically take an instance of {@link PipelineDefinition} which will be defined by the user
 * to declare what interceptors are to be used.
 *
 * @author Nitesh Kant
 */
public interface PipelineFactory {

    /**
     * Returns a list of interceptors to be executed for the passed {@code request}. <br/>
     *
     * @param request Request for which the interceptor list is to be created.
     *
     * @return The list of interceptors for the passed request.
     */
    List<Interceptor> getInterceptors(FullHttpRequest request);
}
