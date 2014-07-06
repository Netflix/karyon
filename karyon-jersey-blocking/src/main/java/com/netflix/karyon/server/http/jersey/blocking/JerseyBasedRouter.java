package com.netflix.karyon.server.http.jersey.blocking;

import com.google.inject.Injector;
import com.netflix.karyon.transport.http.HttpRequestRouter;
import com.sun.jersey.api.container.ContainerFactory;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.guice.spi.container.GuiceComponentProviderFactory;
import com.sun.jersey.spi.container.WebApplication;
import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.io.IOException;

/**
 * @author Nitesh Kant
 */
public class JerseyBasedRouter implements HttpRequestRouter<ByteBuf, ByteBuf> {

    private static final Logger logger = LoggerFactory.getLogger(JerseyBasedRouter.class);

    private final ResourceConfig resourceConfig;
    private final Injector injector;
    private WebApplication application;
    private NettyToJerseyBridge nettyToJerseyBridge;

    public JerseyBasedRouter(ResourceConfig resourceConfig, Injector injector) {
        this.injector = injector;
        if (null == resourceConfig) {
            throw new NullPointerException("Resource config can not be null.");
        }
        this.resourceConfig = resourceConfig;
    }

    public JerseyBasedRouter(ResourceConfig resourceConfig) {
        this(resourceConfig, null);
    }

    @Override
    public Observable<Void> route(HttpServerRequest<ByteBuf> request, HttpServerResponse<ByteBuf> response) {
        try {
            application.handleRequest(nettyToJerseyBridge.bridgeRequest(request, response.getAllocator()),
                                      nettyToJerseyBridge.bridgeResponse(response));
        } catch (IOException e) {
            logger.error("Failed to handle request.", e);
            return Observable.error(e);
        }

        return Observable.empty(); // Since execution is blocking, if this stmt is reached, it means execution is over.
    }

    public void start() {
        NettyContainer container = ContainerFactory.createContainer(NettyContainer.class, resourceConfig,
                                                                    new GuiceComponentProviderFactory(resourceConfig,
                                                                                                      injector));
        application = container.getApplication();
        nettyToJerseyBridge = container.getNettyToJerseyBridge();
        logger.info("Started Jersey based request router.");
    }

    public void stop() {
        logger.info("Stopped Jersey based request router.");
    }
}
