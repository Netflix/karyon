package com.netflix.karyon.server.http.interceptor;

import com.google.common.base.Preconditions;
import com.google.common.collect.Multimap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Nitesh Kant
 */
public class PipelineFactoryImpl<I extends HttpObject, O extends HttpObject> implements PipelineFactory<I, O> {

    private static final Logger logger = LoggerFactory.getLogger(PipelineFactoryImpl.class);

    private final Multimap<PipelineDefinition.Key, InboundInterceptor<I, O>> inboundInterceptors;
    private final Multimap<PipelineDefinition.Key, OutboundInterceptor<O>> outboundInterceptors;

    public PipelineFactoryImpl(@NotNull PipelineDefinition<I, O> pipelineDefinition) {
        Preconditions.checkNotNull(pipelineDefinition, "Pipeline definition can not be null.");
        inboundInterceptors = pipelineDefinition.getInboundInterceptors();
        outboundInterceptors = pipelineDefinition.getOutboundInterceptors();
        Preconditions.checkNotNull(inboundInterceptors, "Inbound interceptors returned by pipeline definition can not be null.");
        Preconditions.checkNotNull(outboundInterceptors, "Outbound interceptors returned by pipeline definition can not be null.");
    }

    @Override
    public List<InboundInterceptor<I, O>> getInboundInterceptors(HttpRequest request, ChannelHandlerContext handlerContext) {
        // TODO: See if this can be cached.
        PipelineDefinition.Key.KeyEvaluationContext ctx = new PipelineDefinition.Key.KeyEvaluationContext(handlerContext);
        List<InboundInterceptor<I, O>> applicableInterceptors = new ArrayList<InboundInterceptor<I, O>>();
        for (Map.Entry<PipelineDefinition.Key, InboundInterceptor<I, O>> interceptorEntry : inboundInterceptors.entries()) {
            if (interceptorEntry.getKey().apply(request, ctx)) {
                applicableInterceptors.add(interceptorEntry.getValue());
            } else if (logger.isDebugEnabled()) {
                logger.debug("Ignoring inbound interceptor {} as the key {} does not apply.",
                             interceptorEntry.getValue().getClass().getName(), interceptorEntry.getKey());
            }
        }
        return applicableInterceptors;
    }

    @Override
    public List<OutboundInterceptor<O>> getOutboundInterceptors(HttpRequest request, ChannelHandlerContext handlerContext) {
        // TODO: See if this can be cached.
        PipelineDefinition.Key.KeyEvaluationContext ctx = new PipelineDefinition.Key.KeyEvaluationContext(handlerContext);
        List<OutboundInterceptor<O>> applicableInterceptors = new ArrayList<OutboundInterceptor<O>>();
        for (Map.Entry<PipelineDefinition.Key, OutboundInterceptor<O>> interceptorEntry : outboundInterceptors.entries()) {
            if (interceptorEntry.getKey().apply(request, ctx)) {
                applicableInterceptors.add(interceptorEntry.getValue());
            } else if (logger.isDebugEnabled()) {
                logger.debug("Ignoring outbound interceptor {} as the key {} does not apply.",
                             interceptorEntry.getValue().getClass().getName(), interceptorEntry.getKey());
            }
        }
        return applicableInterceptors;
    }
}
