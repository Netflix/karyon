package com.netflix.karyon.server;

import com.netflix.karyon.server.spi.RequestRouter;
import com.netflix.karyon.server.spi.ResponseWriter;
import com.netflix.karyon.server.spi.ResponseWriterFactory;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.Attribute;
import io.netty.util.concurrent.EventExecutorGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is mostly used by {@link KaryonNettyServer} to manipulate netty pipeline to add handlers which can only be
 * added after the protocol handling is over. In other words they need any state created during protocol processing. <br/>
 * There are two ways of solving this problem (of a handler using state from protocol processing):
 * <ul>
 <li>Have the state stored as an {@link Attribute} and have the handler *assume* that someone has added that state
 before it is executed.</li>
 <li>Create the handlers with that state dynamically after the protocol processing is done.</li>
 </ul>
 *
 * Karyon takes the latter approach to handlers that require {@link ResponseWriter} instance that can only be created
 * after a {@link ChannelHandlerContext} is created. This approach although a little more complex is more predictable
 * and outright.
 * A typical example of such a handler is {@link RoutingNettyHandler}
 *
 * @param <I> Input object type.
 * @param <O> Output object type.
 */
public abstract class ApplicationPipelineConfigurator <I, O> {

    protected static final Logger LOGGER = LoggerFactory.getLogger(ApplicationPipelineConfigurator.class);

    public static final String ROUTING_HANDLER_NAME = "router";

    private final EventExecutorGroup routerExecutorGroup;
    private final ResponseWriterFactory<O> responseWriterFactory;
    private final RequestRouter<I, O> router;
    private final ProcessingTaskRegistry taskRegistry;
    private final Class<I> inputType;

    protected ApplicationPipelineConfigurator(EventExecutorGroup routerExecutorGroup,
                                              ResponseWriterFactory<O> responseWriterFactory,
                                              RequestRouter<I, O> router, ProcessingTaskRegistry taskRegistry,
                                              Class<I> inputType) {
        this.routerExecutorGroup = routerExecutorGroup;
        this.responseWriterFactory = responseWriterFactory;
        this.router = router;
        this.taskRegistry = taskRegistry;
        this.inputType = inputType;
    }

    protected void configurePipeline(@SuppressWarnings("unused") ChannelPipeline channelPipeline,
                                     @SuppressWarnings("unused") ResponseWriter<O> responseWriter) {
        // No Op by default.
    }

    static class ApplicationPipelineInitiatingHandler<I, O> extends SimpleChannelInboundHandler<I> {

        public static final String HANDLER_NAME="AppPipelineInitiatingHandler";

        private final ApplicationPipelineConfigurator<I, O> configurator;

        private ApplicationPipelineInitiatingHandler(ApplicationPipelineConfigurator<I, O> configurator) {
            super(configurator.inputType, false);
            this.configurator = configurator;
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, I msg) throws Exception {
            configurePipeline(ctx);
            ctx.fireChannelRead(msg);
            ctx.pipeline().remove(HANDLER_NAME); // We need to add the routing handler to the pipeline once per channel.
        }

        static <I, O> void addNewInstanceToPipeline(ChannelPipeline pipeline,
                                                    ApplicationPipelineConfigurator<I, O> configurator) {
            ApplicationPipelineInitiatingHandler<I, O> handler = new ApplicationPipelineInitiatingHandler<I, O>(configurator);
            pipeline.addLast(HANDLER_NAME, handler);
        }

        private void configurePipeline(ChannelHandlerContext context) {
            /**
             * The following code can NOT create a response writer instance with the context of this handler, for the
             * following reason:
             *
             * {@link ChannelHandlerContext} is ALWAYS specific to a single handler in the pipeline, every handler has
             * an exclusive context instance.
             * The pitfall of the above is that if you fire an event on any context instance, the pipeline executes from
             * that handler and NOT from whichever handler the event is fired.
             * Now, if we create the response writer instance using this handler's context, then when the application
             * fires a write the pipeline will execute from this handler onwards which will skip any handlers between
             * the router and this handler.
             * In order to avoid the above case, we create the reponse writer with the context of the router, which we
             * can only get after adding the router to the pipeline. This is the reason why the routing handler is
             * created in two steps, i.e., first without the writer & then after adding the router to the pipeline,
             * we set the writer to the handler.
             */
            final RoutingNettyHandler<I, O> routingNettyHandler =
                    new RoutingNettyHandler<I, O>(configurator.router, configurator.taskRegistry,
                                                  configurator.inputType);
            context.pipeline().addLast(configurator.routerExecutorGroup /*The executor group is nullable*/,
                                       ROUTING_HANDLER_NAME, routingNettyHandler);

            ChannelHandlerContext routerContext = context.pipeline().context(routingNettyHandler);
            ResponseWriter<O> responseWriter = configurator.responseWriterFactory.newWriter(routerContext);
            routingNettyHandler.setResponseWriter(responseWriter);

            configurator.configurePipeline(context.pipeline(), responseWriter);
        }
    }
}
