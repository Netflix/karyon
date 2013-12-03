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
 * A {@link PipelineDefinition} builder that provides various criterions for which an {@link InboundInterceptor}
 * or a {@link OutboundInterceptor} instance can be attached to request processing.
 *
 * @author Nitesh Kant
 */
public class InterceptorPipelineBuilder {

    private final ListMultimap<PipelineDefinition.Key, InboundInterceptor> inboundInterceptors =
            Multimaps.newListMultimap(new LinkedHashMap<PipelineDefinition.Key, Collection<InboundInterceptor>>(),
                                      new Supplier<ArrayList<InboundInterceptor>>() {
                                          @Override
                                          public ArrayList<InboundInterceptor> get() {
                                              return new ArrayList<InboundInterceptor>();
                                          }
                                      });

    private final ListMultimap<PipelineDefinition.Key, OutboundInterceptor> outboundInterceptors =
            Multimaps.newListMultimap(new LinkedHashMap<PipelineDefinition.Key, Collection<OutboundInterceptor>>(),
                                      new Supplier<ArrayList<OutboundInterceptor>>() {
                                          @Override
                                          public ArrayList<OutboundInterceptor> get() {
                                              return new ArrayList<OutboundInterceptor>();
                                          }
                                      });

    public InterceptorPipelineBuilder interceptIfUriForRegex(String regex, InboundInterceptor interceptor) {
        inboundInterceptors.put(new RegexUriConstraintKey(regex), interceptor);
        return this;
    }

    public InterceptorPipelineBuilder interceptIfUri(String constraint, InboundInterceptor interceptor) {
        inboundInterceptors.put(new ServletStyleUriConstraintKey(constraint, ""), interceptor);
        return this;
    }

    public InterceptorPipelineBuilder interceptIfMethod(HttpMethod method, InboundInterceptor interceptor) {
        inboundInterceptors.put(new MethodConstraintKey(method), interceptor);
        return this;
    }

    public InterceptorPipelineBuilder addIntereceptorMapping(PipelineDefinition.Key constraint, InboundInterceptor... interceptors) {
        for (InboundInterceptor interceptor : interceptors) {
            inboundInterceptors.put(constraint, interceptor);
        }
        return this;
    }

    public InterceptorPipelineBuilder interceptIfUriForRegex(String regex, OutboundInterceptor interceptor) {
        outboundInterceptors.put(new RegexUriConstraintKey(regex), interceptor);
        return this;
    }

    public InterceptorPipelineBuilder interceptIfUri(String constraint, OutboundInterceptor interceptor) {
        outboundInterceptors.put(new ServletStyleUriConstraintKey(constraint, ""), interceptor);
        return this;
    }

    public InterceptorPipelineBuilder interceptIfMethod(HttpMethod method, OutboundInterceptor interceptor) {
        outboundInterceptors.put(new MethodConstraintKey(method), interceptor);
        return this;
    }

    public InterceptorPipelineBuilder addIntereceptorMapping(PipelineDefinition.Key constraint, OutboundInterceptor... interceptors) {
        for (OutboundInterceptor interceptor : interceptors) {
            outboundInterceptors.put(constraint, interceptor);
        }
        return this;
    }

    public InterceptorPipelineBuilder interceptIfUriForRegex(String regex, BidirectionalInterceptorAdapter interceptor) {
        interceptIfUriForRegex(regex, (InboundInterceptor) interceptor);
        interceptIfUriForRegex(regex, (OutboundInterceptor) interceptor);
        return this;
    }

    public InterceptorPipelineBuilder interceptIfUri(String constraint, BidirectionalInterceptorAdapter interceptor) {
        interceptIfUri(constraint, (InboundInterceptor) interceptor);
        interceptIfUri(constraint, (OutboundInterceptor) interceptor);
        return this;
    }

    public InterceptorPipelineBuilder interceptIfMethod(HttpMethod method, BidirectionalInterceptorAdapter interceptor) {
        interceptIfMethod(method, (InboundInterceptor) interceptor);
        interceptIfMethod(method, (OutboundInterceptor) interceptor);
        return this;
    }

    public InterceptorPipelineBuilder addIntereceptorMapping(PipelineDefinition.Key constraint, BidirectionalInterceptorAdapter... interceptors) {
        for (BidirectionalInterceptorAdapter interceptor : interceptors) {
            outboundInterceptors.put(constraint, interceptor);
            inboundInterceptors.put(constraint, interceptor);
        }
        return this;
    }

    public PipelineDefinition build() {
        final ImmutableMultimap<PipelineDefinition.Key, InboundInterceptor> inDefn = ImmutableMultimap.copyOf(
                inboundInterceptors);
        final ImmutableMultimap<PipelineDefinition.Key, OutboundInterceptor> outDefn = ImmutableMultimap.copyOf(
                outboundInterceptors);

        return new PipelineDefinition() {

            @Override
            public Multimap<Key, InboundInterceptor> getInboundInterceptors() {
                return inDefn;
            }

            @Override
            public Multimap<Key, OutboundInterceptor> getOutboundInterceptors() {
                return outDefn;
            }
        };
    }

    public PipelineFactory buildFactory() {
        return new PipelineFactoryImpl(build());
    }


}
