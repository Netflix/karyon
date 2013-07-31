package com.netflix.karyon.server.netty.jersey.blocking;

import com.netflix.karyon.server.netty.spi.HttpRequestRouter;
import com.sun.jersey.api.container.ContainerFactory;

/**
 * @author Nitesh Kant
 */
public class JerseyRouterProvider {

    public static HttpRequestRouter createRouter() {
        return ContainerFactory.createContainer(NettyContainer.class, new PropertiesBasedResourceConfig());
    }
}
