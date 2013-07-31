package com.netflix.karyon.server.netty.jersey.blocking;

import com.sun.jersey.api.core.PackagesResourceConfig;

/**
 * @author Nitesh Kant
 */
public class PropertiesBasedResourceConfig extends PackagesResourceConfig {

    // TODO: Bridge archaius with this resource config.

    public PropertiesBasedResourceConfig() {
        super("com.netflix");
    }
}
