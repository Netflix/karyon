package com.netflix.karyon.server.http.jersey.blocking;

import com.sun.jersey.spi.container.WebApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Nitesh Kant
 */
public class NettyContainer {

    private static final Logger logger = LoggerFactory.getLogger(NettyContainer.class);

    private final WebApplication application;
    private final NettyToJerseyBridge nettyToJerseyBridge;

    public NettyContainer(WebApplication application) {
        this.application = application;
        nettyToJerseyBridge = new NettyToJerseyBridge(application);
    }

    NettyToJerseyBridge getNettyToJerseyBridge() {
        return nettyToJerseyBridge;
    }

    WebApplication getApplication() {
        return application;
    }
}
