package com.netflix.karyon.server.spi;

import com.google.common.base.Preconditions;
import com.netflix.karyon.server.ActiveConnectionsCounter;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.EventExecutorGroup;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Default implementation for {@link ChannelPipelineConfigurator}.
 *
 * <h2> Configurator composition </h2>
 * This handler supports configurator composition, so one can pass in multiple other {@link PipelineConfigurator}
 * instances to this configurator, which will be called in sequence after this configurator configures the pipeline.
 *
 * @author Nitesh Kant
 */
public class DefaultChannelPipelineConfigurator<I, O> implements ChannelPipelineConfigurator<I, O> {

    /**
     * {@link LoggingHandler} always added as the first handler in the pipeline.
     */
    public static final String NETTY_LOGGING_HANDLER_NAME = "netty_logging_handler";

    public static final String ACTIVE_CONNECTIONS_COUNTER_NAME = "active_connections_counter";

    private final LoggingHandler loggingHandler; /*One instance for all channels as it is shareable.*/
    private final ActiveConnectionsCounter activeConnectionsCounter; /*One instance for all channels as it is shareable.*/
    private final List<PipelineConfigurator> otherConfigurators;

    @Nullable protected RequestRouter<I, O> router;
    @Nullable protected EventExecutorGroup routerExecutorGroup;

    /**
     * Creates a new pipeline configurator.
     *
     * @param serverName Name of the server.
     * @param nettyLoggerLevel Log level at which the netty's internal logger should be logging. The netty logger will
     *                         log ALL its messages at this level.
     */
    public DefaultChannelPipelineConfigurator(String serverName, LogLevel nettyLoggerLevel) {
        this(serverName, nettyLoggerLevel, (PipelineConfigurator) null);
    }

    /**
     * Creates a new pipeline configurator as a composite of passed {@code }.
     *
     * @param serverName Name of the server.
     * @param nettyLoggerLevel Log level at which the netty's internal logger should be logging. The netty logger will
     *                         log ALL its messages at this level.
     */
    public DefaultChannelPipelineConfigurator(String serverName, LogLevel nettyLoggerLevel,
                                              PipelineConfigurator... otherConfigurators) {
        LogLevel logLevel = null == nettyLoggerLevel ? LogLevel.DEBUG : nettyLoggerLevel;
        this.otherConfigurators = null == otherConfigurators || otherConfigurators.length == 0
                                  ? Collections.<PipelineConfigurator>emptyList()
                                  : Arrays.asList(otherConfigurators);
        loggingHandler = new LoggingHandler(logLevel);
        activeConnectionsCounter = new ActiveConnectionsCounter(serverName);
    }

    @Override
    public void configureNewPipeline(ChannelPipeline channelPipeline) {
        channelPipeline.addFirst(ACTIVE_CONNECTIONS_COUNTER_NAME, activeConnectionsCounter)
                       .addLast(NETTY_LOGGING_HANDLER_NAME, loggingHandler);
        for (PipelineConfigurator otherConfigurator : otherConfigurators) {
            otherConfigurator.configureNewPipeline(channelPipeline);
        }
    }

    @Override
    public RequestRouter<I, O> getRouter() {
        return router;
    }

    @Nullable
    @Override
    public EventExecutorGroup getRouterExecutorGroup() {
        return routerExecutorGroup;
    }

    @Override
    public void setRouter(RequestRouter<I, O> router) {
        Preconditions.checkNotNull(router, "Router can not be null.");
        this.router = router;
    }

    @Override
    public void setRouter(BlockingRequestRouter<I, O> router, @Nullable EventExecutorGroup routerExecutorGroup) {
        setRouter(router);
        this.routerExecutorGroup = routerExecutorGroup;
    }

    /**
     * Additional simplistic pipeline configurator to support easier composition of channel pipeline configurations.
     *
     */
    public interface PipelineConfigurator {

        void configureNewPipeline(ChannelPipeline channelPipeline);

    }

}
