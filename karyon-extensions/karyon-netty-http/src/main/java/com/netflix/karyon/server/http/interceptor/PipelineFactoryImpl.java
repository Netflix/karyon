package com.netflix.karyon.server.http.interceptor;

import com.google.common.base.Preconditions;
import com.google.common.collect.Multimap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Nitesh Kant
 */
public class PipelineFactoryImpl implements PipelineFactory {

    private static final Logger logger = LoggerFactory.getLogger(PipelineFactoryImpl.class);

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
    public List<InboundInterceptor> getInboundInterceptors(FullHttpRequest request,
                                                           ChannelHandlerContext handlerContext) {
        // TODO: See if this can be cached.
        PipelineDefinition.Key.KeyEvaluationContext ctx = new PipelineDefinition.Key.KeyEvaluationContext(handlerContext);
        List<InboundInterceptor> applicableInterceptors = new ArrayList<InboundInterceptor>();
        for (Map.Entry<PipelineDefinition.Key, InboundInterceptor> interceptorEntry : inboundInterceptors.entries()) {
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
    public List<OutboundInterceptor> getOutboundInterceptors(FullHttpRequest request,
                                                             ChannelHandlerContext handlerContext) {
        // TODO: See if this can be cached.
        PipelineDefinition.Key.KeyEvaluationContext ctx = new PipelineDefinition.Key.KeyEvaluationContext(handlerContext);
        List<OutboundInterceptor> applicableInterceptors = new ArrayList<OutboundInterceptor>();
        for (Map.Entry<PipelineDefinition.Key, OutboundInterceptor> interceptorEntry : outboundInterceptors.entries()) {
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
