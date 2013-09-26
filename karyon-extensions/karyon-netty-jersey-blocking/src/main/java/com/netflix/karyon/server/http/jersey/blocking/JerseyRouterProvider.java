package com.netflix.karyon.server.http.jersey.blocking;

import com.netflix.karyon.server.http.spi.HttpRequestRouter;

/**
 * @author Nitesh Kant
 */
public class JerseyRouterProvider {

    public static HttpRequestRouter createRouter() {
        return new JerseyBasedRouter(new PropertiesBasedResourceConfig());
    }
}
