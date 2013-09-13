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

    private final Multimap<PipelineDefinition.Key, InboundInterceptor> inboundInterceptors;
    private final Multimap<PipelineDefinition.Key, OutboundInterceptor> outboundInterceptors;

    public PipelineFactoryImpl(@NotNull PipelineDefinition pipelineDefinition) {
        Preconditions.checkNotNull(pipelineDefinition, "Pipeline definition can not be null.");
        inboundInterceptors = pipelineDefinition.getInboundInterceptors();
        outboundInterceptors = pipelineDefinition.getOutboundInterceptors();
        Preconditions.checkNotNull(inboundInterceptors, "Inbound interceptors returned by pipeline definition can not be null.");
        Preconditions.checkNotNull(outboundInterceptors, "Outbound interceptors returned by pipeline definition can not be null.");
    }

    @Override
    public List<InboundInterceptor> getInboundInterceptors(FullHttpRequest request) {
        // TODO: See if this can be cached.
        PipelineDefinition.Key.KeyEvaluationContext ctx = new PipelineDefinition.Key.KeyEvaluationContext();
        List<InboundInterceptor> applicableInterceptors = new ArrayList<InboundInterceptor>();
        for (Map.Entry<PipelineDefinition.Key, InboundInterceptor> interceptorEntry : inboundInterceptors.entries()) {
            if (interceptorEntry.getKey().apply(request, ctx)) {
                applicableInterceptors.add(interceptorEntry.getValue());
            }
        }
        return applicableInterceptors;
    }

    @Override
    public List<OutboundInterceptor> getOutboundInterceptors(FullHttpRequest request) {
        // TODO: See if this can be cached.
        PipelineDefinition.Key.KeyEvaluationContext ctx = new PipelineDefinition.Key.KeyEvaluationContext();
        List<OutboundInterceptor> applicableInterceptors = new ArrayList<OutboundInterceptor>();
        for (Map.Entry<PipelineDefinition.Key, OutboundInterceptor> interceptorEntry : outboundInterceptors.entries()) {
            if (interceptorEntry.getKey().apply(request, ctx)) {
                applicableInterceptors.add(interceptorEntry.getValue());
            }
        }
        return applicableInterceptors;
    }
}
