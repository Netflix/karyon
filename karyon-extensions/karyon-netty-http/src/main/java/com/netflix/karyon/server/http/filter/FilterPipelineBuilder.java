package com.netflix.karyon.server.http.filter;

import io.netty.handler.codec.http.FullHttpRequest;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *
 *
 * @author Nitesh Kant
 */
public class FilterPipelineBuilder {

    private Map<PipelineDefinition.Key, Filter> filters = new HashMap<PipelineDefinition.Key, Filter>();

    public FilterPipelineBuilder filter(String constraint, Filter filter) {
        filters.put(new PipelineDefinition.Key() {
            @Override
            public boolean apply(FullHttpRequest request) {
                //TODO: Implement filtering
                return true;
            }
        }, filter);
        return this;
    }

    public PipelineDefinition build() {
        final Map<PipelineDefinition.Key, Filter> filterDefn = Collections.unmodifiableMap(filters);
        return new PipelineDefinition() {
            @Override
            public Map<Key, Filter> getFilters() {
                return filterDefn;
            }
        };
    }

    public PipelineFactory buildFactory() {
        return new PipelineFactoryImpl(build());
    }
}
