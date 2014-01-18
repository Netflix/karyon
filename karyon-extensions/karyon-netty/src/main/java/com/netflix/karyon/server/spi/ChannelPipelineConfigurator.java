package com.netflix.karyon.server.spi;

import com.netflix.karyon.server.KaryonNettyServer;
import io.netty.channel.ChannelPipeline;
import io.netty.util.concurrent.EventExecutorGroup;

import javax.annotation.Nullable;

/**
 * An abstraction to control the configuration of the underlying netty pipeline. <br/>
 * <b>All implementations must be thread-safe</b>
 *
 * @author Nitesh Kant
 */
public interface ChannelPipelineConfigurator<I, O> {

    /**
     * This method is called whenever a new client connects to this server & hence a new pipeline is created.
     *
     * @param channelPipeline The channel pipeline to be configured.
     */
    void configureNewPipeline(ChannelPipeline channelPipeline);

    /**
     * Returns the instance of {@link RequestRouter} associated with this pipeline. The router is <emp>always</emp>
     * added to the end of the pipeline. <br/>
     * The reason why this is not left to the {@link #configureNewPipeline(ChannelPipeline)} to add the router is to
     * make sure that router is always added to the end. <br/>
     * <b> This method is NOT called for every request but at the creation of {@link KaryonNettyServer}</b>
     *
     * @return The router instance to be added to the pipeline.
     */
    RequestRouter<I, O> getRouter();

    /**
     * If the {@link RequestRouter} returned by {@link #getRouter()} is blocking in nature & the server is non-blocking
     * then the server requires an executor to run the router. In such a case, this method should return the required
     * {@link EventExecutorGroup} <br/>
     * This will ONLY be called in the above scenario, so if the implementations never returns a blocking router, they
     * can safely return {@code null} here.
     * <b> This method is NOT called for every request but at the creation of {@link KaryonNettyServer}</b>
     *
     * @return The event executor group. {@code null} if the router is non-blocking.
     */
    @Nullable
    EventExecutorGroup getRouterExecutorGroup();

    /**
     * Since, its convenient to be able to use a default {@link ChannelPipelineConfigurator} and still be able to use
     * a different router, this setter method is provided here. <br/>
     *
     * @param router Router to be returned from {@link #getRouter()}
     */
    void setRouter(RequestRouter<I, O> router);

    /**
     * A way to specify an {@link EventExecutorGroup} for a {@link BlockingRequestRouter}. If the executor group is not
     * required, the method {@link #setRouter(RequestRouter)} will be more convenient.
     *
     * @param router Router to be returned from {@link #getRouter()}
     * @param routerExecutorGroup {@link EventExecutorGroup} to be returned by {@link #getRouterExecutorGroup()}
     */
    void setRouter(BlockingRequestRouter<I, O> router, @Nullable EventExecutorGroup routerExecutorGroup);
}
