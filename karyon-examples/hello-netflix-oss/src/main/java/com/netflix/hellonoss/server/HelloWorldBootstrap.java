package com.netflix.hellonoss.server;

import com.google.common.collect.Maps;
import com.netflix.governator.guice.LifecycleInjectorBuilder;
import com.netflix.karyon.server.ServerBootstrap;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;

import java.util.Map;

/**
 * @author Nitesh Kant
 */
public class HelloWorldBootstrap extends ServerBootstrap {

    @Override
    protected void beforeInjectorCreation(LifecycleInjectorBuilder builderToBeUsed) {
        builderToBeUsed.withAdditionalModules(new JerseyServletModule() {
            @Override
            protected void configureServlets() {
                Map<String, String> params = Maps.newHashMap();
                params.put(PackagesResourceConfig.PROPERTY_PACKAGES, "com.netflix.hellonoss");
                serve("/rest/v1/*").with(GuiceContainer.class, params);
                binder().bind(GuiceContainer.class).asEagerSingleton();
            }
        });
    }
}
