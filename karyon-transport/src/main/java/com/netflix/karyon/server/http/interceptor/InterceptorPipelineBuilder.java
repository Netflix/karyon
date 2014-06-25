package com.netflix.karyon.server.http.interceptor;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;

/**
 * A {@link PipelineDefinition} builder that provides various criterions for which an {@link InboundInterceptor}
 * or a {@link OutboundInterceptor} instance can be attached to request processing.
 *
 * @author Nitesh Kant
 */
public class InterceptorPipelineBuilder<I extends HttpObject, O extends HttpObject> {

    private final ListMultimap<PipelineDefinition.Key, InboundInterceptor<I, O>> inboundInterceptors =
            Multimaps.newListMultimap(new LinkedHashMap<PipelineDefinition.Key, Collection<InboundInterceptor<I, O>>>(),
                                      new Supplier<ArrayList<InboundInterceptor<I, O>>>() {
                                          @Override
                                          public ArrayList<InboundInterceptor<I, O>> get() {
                                              return new ArrayList<InboundInterceptor<I, O>>();
                                          }
                                      });

    private final ListMultimap<PipelineDefinition.Key, OutboundInterceptor<O>> outboundInterceptors =
            Multimaps.newListMultimap(new LinkedHashMap<PipelineDefinition.Key, Collection<OutboundInterceptor<O>>>(),
                                      new Supplier<ArrayList<OutboundInterceptor<O>>>() {
                                          @Override
                                          public ArrayList<OutboundInterceptor<O>> get() {
                                              return new ArrayList<OutboundInterceptor<O>>();
                                          }
                                      });

    public InterceptorPipelineBuilder<I, O> interceptIfUriForRegex(String regex, InboundInterceptor<I, O> interceptor) {
        inboundInterceptors.put(new RegexUriConstraintKey(regex), interceptor);
        return this;
    }

    public InterceptorPipelineBuilder<I, O> interceptIfUri(String constraint, InboundInterceptor<I, O> interceptor) {
        inboundInterceptors.put(new ServletStyleUriConstraintKey(constraint, ""), interceptor);
        return this;
    }

    public InterceptorPipelineBuilder<I, O> interceptIfMethod(HttpMethod method, InboundInterceptor<I, O> interceptor) {
        inboundInterceptors.put(new MethodConstraintKey(method), interceptor);
        return this;
    }

    public InterceptorPipelineBuilder<I, O> addIntereceptorMapping(PipelineDefinition.Key constraint, InboundInterceptor<I, O>... interceptors) {
        for (InboundInterceptor<I, O> interceptor : interceptors) {
            inboundInterceptors.put(constraint, interceptor);
        }
        return this;
    }

    public InterceptorPipelineBuilder<I, O> interceptIfUriForRegex(String regex, OutboundInterceptor<O> interceptor) {
        outboundInterceptors.put(new RegexUriConstraintKey(regex), interceptor);
        return this;
    }

    public InterceptorPipelineBuilder<I, O> interceptIfUri(String constraint, OutboundInterceptor<O> interceptor) {
        outboundInterceptors.put(new ServletStyleUriConstraintKey(constraint, ""), interceptor);
        return this;
    }

    public InterceptorPipelineBuilder<I, O> interceptIfMethod(HttpMethod method, OutboundInterceptor<O> interceptor) {
        outboundInterceptors.put(new MethodConstraintKey(method), interceptor);
        return this;
    }

    public InterceptorPipelineBuilder<I, O> addIntereceptorMapping(PipelineDefinition.Key constraint, OutboundInterceptor<O>... interceptors) {
        for (OutboundInterceptor<O> interceptor : interceptors) {
            outboundInterceptors.put(constraint, interceptor);
        }
        return this;
    }

    public PipelineDefinition<I, O> build() {
        final ImmutableMultimap<PipelineDefinition.Key, InboundInterceptor<I, O>> inDefn = ImmutableMultimap.copyOf(
                inboundInterceptors);
        final ImmutableMultimap<PipelineDefinition.Key, OutboundInterceptor<O>> outDefn = ImmutableMultimap.copyOf(
                outboundInterceptors);

        return new PipelineDefinition<I, O>() {

            @Override
            public Multimap<Key, InboundInterceptor<I, O>> getInboundInterceptors() {
                return inDefn;
            }

            @Override
            public Multimap<Key, OutboundInterceptor<O>> getOutboundInterceptors() {
                return outDefn;
            }
        };
    }

    public PipelineFactory<I, O> buildFactory() {
        return new PipelineFactoryImpl<I, O>(build());
    }
}
