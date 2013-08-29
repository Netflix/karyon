package com.netflix.karyon.server.http.interceptor;

import com.google.common.base.Preconditions;
import com.google.common.collect.Multimap;
import io.netty.handler.codec.http.FullHttpRequest;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Nitesh Kant
 */
public class PipelineFactoryImpl implements PipelineFactory {

    private final Multimap<PipelineDefinition.Key, Interceptor> filters;

    public PipelineFactoryImpl(@NotNull PipelineDefinition pipelineDefinition) {
        Preconditions.checkNotNull(pipelineDefinition, "Pipeline definition can not be null.");
        filters = pipelineDefinition.getInterceptors();
        Preconditions.checkNotNull(filters, "Filters returned by pipeline definition can not be null.");
    }

    @Override
    public List<Interceptor> getInterceptors(FullHttpRequest request) {
        // TODO: See if this can be cached.
        List<Interceptor> applicableInterceptors = new ArrayList<Interceptor>();
        for (Map.Entry<PipelineDefinition.Key, Interceptor> filterEntry : filters.entries()) {
            if (filterEntry.getKey().apply(request)) {
                applicableInterceptors.add(filterEntry.getValue());
            }
        }
        return applicableInterceptors;
    }
}
