package com.netflix.karyon.server.http.filter;

import com.google.common.base.Preconditions;
import io.netty.handler.codec.http.FullHttpRequest;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Nitesh Kant
 */
public class PipelineFactoryImpl implements PipelineFactory {

    private final Map<PipelineDefinition.Key,Filter> filters;

    public PipelineFactoryImpl(@NotNull PipelineDefinition pipelineDefinition) {
        Preconditions.checkNotNull(pipelineDefinition, "Pipeline definition can not be null.");
        filters = pipelineDefinition.getFilters();
        Preconditions.checkNotNull(filters, "Filters returned by pipeline definition can not be null.");
    }

    @Override
    public List<Filter> getFilters(FullHttpRequest request) {
        // TODO: See if this can be cached.
        List<Filter> applicableFilters = new ArrayList<Filter>();
        for (Map.Entry<PipelineDefinition.Key, Filter> filterEntry : filters.entrySet()) {
            if (filterEntry.getKey().apply(request)) {
                applicableFilters.add(filterEntry.getValue());
            }
        }
        return applicableFilters;
    }
}
