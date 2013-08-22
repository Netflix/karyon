package com.netflix.karyon.server.http.filter;

import io.netty.handler.codec.http.FullHttpRequest;

import java.util.List;

/**
 * A factory that is invoked at the time of request execution to get the list of filters for a particular request. <br/>
 * An implementation will typically take an instance of {@link PipelineDefinition} which will be defined by the user
 * to declare what filters are to be used.
 *
 * @author Nitesh Kant
 */
public interface PipelineFactory {

    /**
     * Returns a list of filters to be executed for the passed {@code request}. <br/>
     *
     * @param request Request for which the filter list is to be created.
     *
     * @return The list of filters for the passed request.
     */
    List<Filter> getFilters(FullHttpRequest request);
}
