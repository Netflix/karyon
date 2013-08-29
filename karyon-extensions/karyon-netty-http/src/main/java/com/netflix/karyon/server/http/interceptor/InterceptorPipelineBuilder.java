package com.netflix.karyon.server.http.interceptor;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import io.netty.handler.codec.http.HttpMethod;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;

/**
 * A {@link PipelineDefinition} builder that provides various criterions for which an {@link Interceptor} instance can
 * be attached to a request processing.
 *
 * @author Nitesh Kant
 */
public class InterceptorPipelineBuilder {

    private ListMultimap<PipelineDefinition.Key, Interceptor> filters =
            Multimaps.newListMultimap(new LinkedHashMap<PipelineDefinition.Key, Collection<Interceptor>>(),
                                      new Supplier<ArrayList<Interceptor>>() {
                                          @Override
                                          public ArrayList<Interceptor> get() {
                                              return new ArrayList<Interceptor>();
                                          }
                                      });

    /**
     * Configures an interceptor to be invoked if the HTTP request path follows the passed criterion. <br/>
     * The criterion can be defined as
     *
     * @param constraint Constraint that guards the interceptor execution.
     * @param interceptor Interceptor to invoke if the criterion is satisfied.
     *
     * @return This builder instance.
     */
    public InterceptorPipelineBuilder interceptIfUri(String constraint, Interceptor interceptor) {
        filters.put(new UriConstraintKey(constraint), interceptor);
        return this;
    }

    public InterceptorPipelineBuilder interceptIfMethod(HttpMethod method, Interceptor interceptor) {
        filters.put(new MethodConstraintKey(method), interceptor);
        return this;
    }

    public InterceptorPipelineBuilder addIntereceptorMapping(PipelineDefinition.Key constraint, Interceptor... interceptors) {
        for (Interceptor interceptor : interceptors) {
            filters.put(constraint, interceptor);
        }
        return this;
    }

    public PipelineDefinition build() {
        final ImmutableMultimap<PipelineDefinition.Key, Interceptor> filterDefn = ImmutableMultimap.copyOf(filters);
        return new PipelineDefinition() {
            @Override
            public Multimap<Key, Interceptor> getInterceptors() {
                return filterDefn;
            }
        };
    }

    public PipelineFactory buildFactory() {
        return new PipelineFactoryImpl(build());
    }


}
