package com.netflix.karyon.server.http;

import com.netflix.karyon.server.KaryonNettyServer;
import com.netflix.karyon.server.spi.DefaultChannelPipelineConfigurator;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.LastHttpContent;

/**
 * Basic HTTP pipeline configurator which adds netty's Http encoder and decoder to the pipeline. <br/>
 * This is a good place to start if you want to create a pipeline to handle HTTP. <br/>
 *
 * Pipelines created by this class will emit the following objects during an HTTP processing:
 *
 * <h2>Request</h2>
 * One {@link HttpRequest} object. <br/>
 * Zero or more {@link HttpContent} object.<br/>
 * One {@link LastHttpContent} object.<br/>
 *
 * <h2>Response</h2>
 * One {@link HttpResponse} object.<br/>
 * Zero or more {@link HttpContent} object.<br/>
 * One {@link LastHttpContent} object.<br/>
 * <p/>
 * In case of a keep-alive connection, there can be multiple of these request-response cycles but the order remains the
 * same.
 *
 * <h2>Configuration parameters</h2>
 * This class provides all the configuration options provided by {@link HttpRequestDecoder}, with the following defaults:
 * <table border="1">
 * <tr>
 * <th>Name</th><th>Default</th>
 * </tr>
 * <tr>
 * <td>{@code maxInitialLineLength}</td>
 * <td>{@link #MAX_INITIAL_LINE_LENGTH_DEFAULT}</td>
 * </tr>
 * <tr>
 * <td>{@code maxHeaderSize}</td>
 * <td>{@link #MAX_HEADER_SIZE_DEFAULT}</td>
 * </tr>
 * <tr>
 * <td>{@code maxChunkSize}</td>
 * <td>{@link #MAX_CHUNK_SIZE_DEFAULT}</td>
 * </tr>
 * <tr>
 * <td>{@code validateHeaders}</td>
 * <td>{@link #VALIDATE_HEADERS_DEFAULT}</td>
 * </tr>
 * </table>
 *
 * This must be used with {@link DefaultChannelPipelineConfigurator} to be able to use it to build a
 * {@link KaryonNettyServer} instance.
 *
 * @author Nitesh Kant
 *
 * @see HttpRequestDecoder
 * @see HttpResponseEncoder
 * @see DefaultChannelPipelineConfigurator
 */
public class HttpPipelineConfigurator implements DefaultChannelPipelineConfigurator.PipelineConfigurator {

    public static final String HTTP_DECODER_HANDLER_NAME = "http_decoder";
    public static final String HTTP_ENCODER_HANDLER_NAME = "http_encoder";
    public static final int MAX_INITIAL_LINE_LENGTH_DEFAULT = 4096;
    public static final int MAX_HEADER_SIZE_DEFAULT = 8192;
    public static final int MAX_CHUNK_SIZE_DEFAULT = 8192;
    public static final boolean VALIDATE_HEADERS_DEFAULT = true;

    private final int maxInitialLineLength;
    private final int maxHeaderSize;
    private final int maxChunkSize;
    private final boolean validateHeaders;

    /**
     * Creates a new pipeline configurator with all defaults. See {@link HttpPipelineConfigurator} for details about the
     * defaults.
     */
    public HttpPipelineConfigurator() {
        this(MAX_INITIAL_LINE_LENGTH_DEFAULT, MAX_HEADER_SIZE_DEFAULT, MAX_CHUNK_SIZE_DEFAULT);
    }

    public HttpPipelineConfigurator(int maxInitialLineLength, int maxHeaderSize, int maxChunkSize) {
        this(maxInitialLineLength, maxHeaderSize, maxChunkSize, VALIDATE_HEADERS_DEFAULT);
    }
    public HttpPipelineConfigurator(int maxInitialLineLength, int maxHeaderSize, int maxChunkSize, boolean validateHeaders) {
        this.maxInitialLineLength = maxInitialLineLength;
        this.maxHeaderSize = maxHeaderSize;
        this.maxChunkSize = maxChunkSize;
        this.validateHeaders = validateHeaders;
    }

    @Override
    public void configureNewPipeline(ChannelPipeline channelPipeline) {
        channelPipeline.addLast(HTTP_DECODER_HANDLER_NAME, new HttpRequestDecoder(maxInitialLineLength, maxHeaderSize,
                                                                                  maxChunkSize, validateHeaders))
                       .addLast(HTTP_ENCODER_HANDLER_NAME, new HttpResponseEncoder());
    }
}
