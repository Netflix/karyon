package com.netflix.karyon.server.http.interceptor;

import com.google.common.base.Preconditions;
import com.netflix.karyon.server.http.spi.RequestContextAttributes;
import com.netflix.karyon.server.spi.ResponseWriter;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * A netty channel handler that invokes all the {@link InboundInterceptor} and {@link OutboundInterceptor} configured
 * for the pipeline at appropriate times.
 */
public class InterceptorsNettyHandler<I extends HttpObject, O extends HttpObject> extends
        ChannelDuplexHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(InterceptorsNettyHandler.class);

    public static final String INBOUND_INTERCEPTOR_NETTY_HANDLER = "InterceptorsNettyHandler";

    private final ResponseWriter<O> responseWriter;
    private final Class<I> inputType;
    private final Class<O> outputType;
    @Nullable private NextInterceptorInvoker<I, O> nextInboundInvoker;
    @Nullable private NextInterceptorInvoker<O, O> nextOutboundInvoker;
    private final PipelineFactory<I, O> interceptorFactory;
    private List<InboundInterceptor<I, O>> inboundInterceptors;
    private List<OutboundInterceptor<O>> outboundInterceptors;

    public InterceptorsNettyHandler(@Nullable PipelineFactory<I, O> interceptorFactory, ResponseWriter<O> responseWriter,
                                    Class<I> inputType, Class<O> outputType) {
        Preconditions.checkNotNull(responseWriter, "Response writer can not be null.");
        this.responseWriter = responseWriter;
        this.inputType = inputType;
        this.outputType = outputType;
        this.interceptorFactory = null == interceptorFactory ? new PipelineFactory<I, O>() {
            @Override
            public List<InboundInterceptor<I, O>> getInboundInterceptors(HttpRequest request,
                                                                         ChannelHandlerContext handlerContext) {
                return Collections.emptyList();
            }

            @Override
            public List<OutboundInterceptor<O>> getOutboundInterceptors(HttpRequest request,
                                                                        ChannelHandlerContext handlerContext) {
                return Collections.emptyList();
            }
        } : interceptorFactory;
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {

        if (!inputType.isAssignableFrom(msg.getClass())) {
            LOGGER.error("Unexpected message recieved in the pipeline. Expected: " + inputType.getName() +
                         ", received: " + msg.getClass().getName());
            return; // Passing it ahead is of no good as we know the router will not be able to handle this type.
        }

        if (HttpRequest.class.isAssignableFrom(msg.getClass())) {
            // This means it is the request headers that are read & hence it is a new request which needs to re-init
            // interceptors & used for the rest of the request interception.
            HttpRequest httpRequest = (HttpRequest) msg;

            RequestContextAttributes.populateCommonAttributesFromRequest(httpRequest, ctx);

            inboundInterceptors = interceptorFactory.getInboundInterceptors(httpRequest, ctx);
            outboundInterceptors = interceptorFactory.getOutboundInterceptors(httpRequest, ctx);
        }

        // Since, the invoker is stateful, we need to initialize it for every message recieved. The contract of the
        // interceptors is to be invoked every time the message flows in the pipeline to the router.
        nextInboundInvoker = new InboundNextInterceptorInvoker<I, O>(inboundInterceptors, new InboundInterceptor<I, O>() {

            @Override
            public void interceptIn(I httpRequest, ResponseWriter<O> responseWriter,
                                    NextInterceptorInvoker<I, O> invoker) {
                ctx.fireChannelRead(httpRequest);
            }
        });

        nextInboundInvoker.executeNext((I) msg, responseWriter);
    }

    @Override
    public void write(final ChannelHandlerContext ctx, Object msg, final ChannelPromise promise) throws Exception {

        if (!outputType.isAssignableFrom(msg.getClass())) {
            LOGGER.warn("Unexpected output message recieved in the pipeline. Expected: " + outputType.getName() +
                        ", received: " + msg.getClass().getName());
        }

        // Since, the invoker is stateful, we need to initialize it for every message recieved. The contract of the
        // interceptors is to be invoked every time the message flows in the pipeline to the router.
        nextOutboundInvoker = new OutboundNextInterceptorInvoker<O>(outboundInterceptors, new OutboundInterceptor<O>() {
            @Override
            public void interceptOut(O httpResponse, ResponseWriter<O> responseWriter,
                                     NextInterceptorInvoker<O, O> invoker) {
                ctx.write(httpResponse, promise);
            }
        });

        nextOutboundInvoker.executeNext((O) msg, responseWriter);
    }
}
