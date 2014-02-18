package com.netflix.karyon.server;

import com.google.common.base.Preconditions;
import com.google.common.reflect.TypeToken;
import com.netflix.karyon.server.bootstrap.KaryonBootstrap;
import com.netflix.karyon.server.spi.BlockingRequestRouter;
import com.netflix.karyon.server.spi.ChannelPipelineConfigurator;
import com.netflix.karyon.server.spi.LifecycleAware;
import com.netflix.karyon.server.spi.RequestRouter;
import com.netflix.karyon.server.spi.ResponseWriterFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.concurrent.EventExecutorGroup;
import io.netty.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.netflix.karyon.server.ApplicationPipelineConfigurator.ApplicationPipelineInitiatingHandler.addNewInstanceToPipeline;

/**
 * @author Nitesh Kant
 */
public abstract class KaryonNettyServer<I, O> extends KaryonServer {

    protected final Logger logger = LoggerFactory.getLogger(KaryonNettyServer.class);

    protected final ChannelPipelineConfigurator<I, O> pipelineConfigurator;
    protected final ResponseWriterFactory<O> responseWriterFactory;
    protected final ServerBootstrap bootstrap;
    protected final ProcessingTaskRegistry taskRegistry;
    protected final RequestRouter<I,O> router;

    @Nullable protected final EventExecutorGroup routerExecutorGroup;

    private ChannelFuture serverShutdownFuture;
    protected final Class<I> inputType;
    protected final Class<O> outputType;

    @SuppressWarnings("unchecked")
    protected KaryonNettyServer(@Nonnull ServerBootstrap bootstrap,
                                @Nonnull ChannelPipelineConfigurator<I, O> pipelineConfigurator,
                                @Nonnull ResponseWriterFactory<O> responseWriterFactory,
                                @Nullable KaryonBootstrap karyonBootstrap) {
        super(karyonBootstrap);
        Preconditions.checkNotNull(responseWriterFactory, "Response writer factory can not be null.");
        inputType = (Class<I>) new TypeToken<I>(getClass()) {}.getRawType();
        outputType = (Class<O>) new TypeToken<O>(getClass()) {}.getRawType();
        this.bootstrap = bootstrap;
        this.pipelineConfigurator = pipelineConfigurator;
        this.responseWriterFactory = responseWriterFactory;
        taskRegistry = new ProcessingTaskRegistry();
        router = pipelineConfigurator.getRouter();
        if (RequestRouter.RoutersNatureIdentifier.isBlocking(router) && shouldRunBlockingRouterInAnExecutor()) {
            routerExecutorGroup = pipelineConfigurator.getRouterExecutorGroup();
            Preconditions.checkNotNull(routerExecutorGroup,
                                       "Router executor group can not be null when the server is non-blocking & the router is blocking.");
        } else {
            routerExecutorGroup = null;
        }
    }

    /**
     * Starts this server and blocks the calling thread till the server is stopped. <br/>
     * In case it is not required to block the calling thread, one must instead call
     * {@link #startWithoutWaitingForShutdown()}
     *
     * @throws Exception If the start fails.
     */
    @Override
    public void start() throws Exception {
        super.start();
        serverShutdownFuture.sync();
    }

    public void startWithoutWaitingForShutdown() throws Exception {
        super.start();
    }

    @Override
    protected void internalStart() throws Exception {
        if (RequestRouter.RoutersNatureIdentifier.isLifecycleAware(router)) {
            ((LifecycleAware) router).start();
        }
        bootstrap.childHandler(newChannelInitializer());
        Channel channel = bootstrap.bind().sync().channel();
        logger.info("Started netty based karyon server at port: " + channel.localAddress());
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    stop();
                } catch (Exception e) {
                    logger.error("Error while shutting down.", e);
                }
            }
        }));
        serverShutdownFuture = channel.closeFuture();
    }

    @Override
    protected void internalStop() throws Exception {
        logger.info("Shutting down server.");
        Future<?> acceptorTermFuture = bootstrap.group().shutdownGracefully();
        Future<?> workerTermFuture = bootstrap.childGroup().shutdownGracefully();

        logger.info("Waiting for acceptor threads to stop.");
        acceptorTermFuture.sync();
        logger.info("Waiting for worker threads to stop.");
        workerTermFuture.sync();
        logger.info("Shutdown complete.");
    }

    /**
     * Called if and only if, the configured router is blocking i.e. an instance of {@link BlockingRequestRouter}. <br/>
     * This will be called from withing the constructor of this class & hence should not depend on any other instance
     * state but what is set by this class.
     *
     * @return {@code true} if a blocking router should run within an executor.
     */
    protected abstract boolean shouldRunBlockingRouterInAnExecutor();

    /**
     * Creates a new {@link ChannelInitializer} to be used by this server for all connections.
     *
     * @return The {@link ChannelInitializer} instance.
     */
    protected DefaultChannelInitializer<I, O> newChannelInitializer() {
        return new DefaultChannelInitializer<I, O>(pipelineConfigurator, newApplicationPipelineConfigurator());
    }

    protected ApplicationPipelineConfigurator<I, O> newApplicationPipelineConfigurator() {
        return new ApplicationPipelineConfigurator<I, O>(routerExecutorGroup, responseWriterFactory,
                                                         router, taskRegistry, inputType) {
        };
    }

    protected Class<? super I> getInputType() {
        return inputType;
    }

    protected Class<? super O> getOutputType() {
        return outputType;
    }

    protected class DefaultChannelInitializer<I, O> extends ChannelInitializer<SocketChannel> {

        protected final ChannelPipelineConfigurator<I, O> pipelineConfigurator;
        @Nullable
        private final ApplicationPipelineConfigurator<I, O> applicationPipelineConfigurator;

        public DefaultChannelInitializer(ChannelPipelineConfigurator<I, O> pipelineConfigurator,
                                         @Nullable ApplicationPipelineConfigurator<I, O> applicationPipelineConfigurator) {
            this.pipelineConfigurator = pipelineConfigurator;
            this.applicationPipelineConfigurator = applicationPipelineConfigurator;
        }

        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            pipelineConfigurator.configureNewPipeline(ch.pipeline());
            addNewInstanceToPipeline(ch.pipeline(), applicationPipelineConfigurator);
        }
    }
}
