package com.netflix.karyon.server.netty.jersey.blocking;

import com.netflix.karyon.server.netty.spi.HttpRequestRouter;
import com.netflix.karyon.server.netty.spi.HttpResponseWriter;
import com.sun.jersey.spi.container.WebApplication;
import io.netty.handler.codec.http.FullHttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author Nitesh Kant
 */
public class NettyContainer implements HttpRequestRouter {

    private static final Logger logger = LoggerFactory.getLogger(NettyContainer.class);

    private final WebApplication application;
    private final NettyToJerseyBridge nettyToJerseyBridge;

    public NettyContainer(WebApplication application) {
        this.application = application;
        nettyToJerseyBridge = new NettyToJerseyBridge(application);
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
}
