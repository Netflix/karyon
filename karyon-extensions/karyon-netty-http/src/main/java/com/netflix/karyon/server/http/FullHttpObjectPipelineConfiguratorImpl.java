package com.netflix.karyon.server.http;

import com.google.common.base.Preconditions;
import com.netflix.karyon.server.spi.DefaultChannelPipelineConfigurator;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpObjectAggregator;

/**
 * An implementation of {@link DefaultChannelPipelineConfigurator.PipelineConfigurator} to aggregated multiple parts of
 * an http message. This must be used with {@link DefaultChannelPipelineConfigurator} to handle HTTP protocol.
 *
 * @author Nitesh Kant
 */
public class FullHttpObjectPipelineConfiguratorImpl implements DefaultChannelPipelineConfigurator.PipelineConfigurator {

    public static final String HTTP_AGGREGATOR_HANDLER_NAME = "http_aggregator";
    private final int maxChunkSize;
    private final HttpPipelineConfigurator pipelineConfigurator;

    public FullHttpObjectPipelineConfiguratorImpl(int maxChunkSize, HttpPipelineConfigurator httpConfigurator) {
        Preconditions.checkNotNull(httpConfigurator, "Http configurator is required.");
        this.maxChunkSize = maxChunkSize;
        pipelineConfigurator = httpConfigurator;
    }

    @Override
    public void configureNewPipeline(ChannelPipeline channelPipeline) {
        pipelineConfigurator.configureNewPipeline(channelPipeline);
        channelPipeline.addBefore(HttpPipelineConfigurator.HTTP_ENCODER_HANDLER_NAME, HTTP_AGGREGATOR_HANDLER_NAME,
                                  new HttpObjectAggregator(maxChunkSize));
    }
}
