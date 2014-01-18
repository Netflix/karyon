package com.netflix.karyon.server.http.jersey.blocking;

import com.google.common.base.Preconditions;
import com.netflix.karyon.server.http.spi.BlockingHttpRequestRouter;
import com.netflix.karyon.server.http.spi.StatefulHttpResponseWriter;
import com.netflix.karyon.server.spi.LifecycleAware;
import com.netflix.karyon.server.spi.ResponseWriter;
import com.sun.jersey.api.container.ContainerFactory;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.spi.container.WebApplication;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author Nitesh Kant
 */
public class JerseyBasedRouter implements BlockingHttpRequestRouter<FullHttpRequest, FullHttpResponse>, LifecycleAware {

    private static final Logger logger = LoggerFactory.getLogger(JerseyBasedRouter.class);

    private final ResourceConfig resourceConfig;
    private WebApplication application;
    private NettyToJerseyBridge nettyToJerseyBridge;

    public JerseyBasedRouter(ResourceConfig resourceConfig) {
        Preconditions.checkNotNull(resourceConfig);
        this.resourceConfig = resourceConfig;
    }

    @Override
    public Future<Void> process(FullHttpRequest request, ResponseWriter<FullHttpResponse> responseWriter) {
        Future<Void> processingFuture = new DefaultPromise<Void>(responseWriter.getChannelHandlerContext().executor());
        StatefulHttpResponseWriter statefulWriter = (StatefulHttpResponseWriter) responseWriter;
        try {
            application.handleRequest(nettyToJerseyBridge.bridgeRequest(request),
                                      nettyToJerseyBridge.bridgeResponseWriter(statefulWriter));
        } catch (IOException e) {
            logger.error("Failed to handle request.", e);
            // TODO: Define a default fatal error handler that can send HTTP response.
        }
        return processingFuture;
    }

    @Override
    public void start() {
        NettyContainer container = ContainerFactory.createContainer(NettyContainer.class, resourceConfig);
        application = container.getApplication();
        nettyToJerseyBridge = container.getNettyToJerseyBridge();
        logger.info("Started Jersey based request router.");
    }

    @Override
    public void stop() {
        logger.info("Stopped Jersey based request router.");
    }
}
