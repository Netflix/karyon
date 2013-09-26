package com.netflix.karyon.server.http.jersey.blocking;

import com.google.common.base.Preconditions;
import com.netflix.karyon.server.http.spi.HttpRequestRouter;
import com.netflix.karyon.server.http.spi.HttpResponseWriter;
import com.sun.jersey.api.container.ContainerFactory;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.spi.container.WebApplication;
import io.netty.handler.codec.http.FullHttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author Nitesh Kant
 */
public class JerseyBasedRouter implements HttpRequestRouter {

    private static final Logger logger = LoggerFactory.getLogger(JerseyBasedRouter.class);

    private final ResourceConfig resourceConfig;
    private WebApplication application;
    private NettyToJerseyBridge nettyToJerseyBridge;

    public JerseyBasedRouter(ResourceConfig resourceConfig) {
        Preconditions.checkNotNull(resourceConfig);
        this.resourceConfig = resourceConfig;
    }

    @Override
    public boolean isBlocking() {
        return true;
    }

    @Override
    public void process(FullHttpRequest request, HttpResponseWriter responseWriter) {
        try {
            application.handleRequest(nettyToJerseyBridge.bridgeRequest(request),
                                      nettyToJerseyBridge.bridgeResponseWriter(responseWriter));
        } catch (IOException e) {
            logger.error("Failed to handle request.", e);
            // TODO: Define a default fatal error handler that can send HTTP response.
        }
    }

    @Override
    public void start() {
        NettyContainer container = ContainerFactory.createContainer(NettyContainer.class, resourceConfig);
        application = container.getApplication();
        nettyToJerseyBridge = container.getNettyToJerseyBridge();
    }
}
