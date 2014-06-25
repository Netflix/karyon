package com.netflix.karyon.server.http.interceptor;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;

import java.util.List;

/**
 * A factory that is invoked at the time of request execution to get the list of interceptors for a particular request. <br/>
 * An implementation will typically take an instance of {@link PipelineDefinition} which will be defined by the user
 * to declare what interceptors are to be used.
 *
 * @author Nitesh Kant
 */
public interface PipelineFactory<I extends HttpObject, O extends HttpObject> {

    /**
     * Returns a list of inbound interceptors to be executed for the passed {@code request}. <br/>
     *
     *
     * @param request Request for which the interceptor list is to be created.
     * @param handlerContext Channel handler context for the channel this request is executed.
     *
     * @return The list of inbound interceptors for the passed request.
     */
    List<InboundInterceptor<I, O>> getInboundInterceptors(HttpRequest request, ChannelHandlerContext handlerContext);

    /**
     * Returns a list of outbound interceptors to be executed for the passed {@code request}. <br/>
     *
     *
     * @param request Request for which the interceptor list is to be created.
     * @param handlerContext Channel handler context for the channel this request is executed.
     *
     * @return The list of outbound interceptors for the passed request.
     */
    List<OutboundInterceptor<O>> getOutboundInterceptors(HttpRequest request, ChannelHandlerContext handlerContext);
}
