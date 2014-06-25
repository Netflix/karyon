package com.netflix.karyon.server;

import com.netflix.karyon.server.spi.RequestRouter;
import com.netflix.karyon.server.spi.ResponseWriter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.Future;

/**
 * An adapter between karyon's router & netty's channel pipeline handler.
 *
 * @author Nitesh Kant
 */
class RoutingNettyHandler<I, O> extends SimpleChannelInboundHandler<I> {

    private final RequestRouter<I, O> router;
    private ResponseWriter<O> responseWriter;
    private final ProcessingTaskRegistry taskRegistry;

    /**
     * Package-private constructor that creates a half-initialized handler. Before using this handler, one should call
     * {@link #setResponseWriter(ResponseWriter)}
     */
    RoutingNettyHandler(RequestRouter<I, O> router, ProcessingTaskRegistry taskRegistry, Class<I> inputType) {
        super(inputType, !RequestRouter.RoutersNatureIdentifier.isBlocking(router));
        this.router = router;
        this.taskRegistry = taskRegistry;
    }

    void setResponseWriter(ResponseWriter<O> responseWriter) {
        this.responseWriter = responseWriter;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, I request) throws Exception {
        Future<Void> processFuture = router.process(request, responseWriter);
        taskRegistry.postEnqueueToRouter(processFuture);
    }
}
