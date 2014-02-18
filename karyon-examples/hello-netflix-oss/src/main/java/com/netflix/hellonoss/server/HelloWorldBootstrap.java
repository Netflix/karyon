package com.netflix.hellonoss.server;

import com.google.common.collect.Maps;
import com.netflix.governator.guice.LifecycleInjectorBuilder;
import com.netflix.karyon.governator.KaryonGovernatorBootstrap;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;

import java.util.Map;

/**
 * @author Nitesh Kant
 */
public class HelloWorldBootstrap extends KaryonGovernatorBootstrap {

    protected HelloWorldBootstrap(LifecycleInjectorBuilder lifecycleInjectorBuilder, String... scanPackages) {
        super(lifecycleInjectorBuilder, scanPackages);
    }

    public static HelloWorldBootstrap createNew() {
        return (HelloWorldBootstrap) new Builder() {
            @Override
            protected KaryonGovernatorBootstrap newBootstrapInstance(LifecycleInjectorBuilder lifecycleInjectorBuilder,
                                                                     String[] scanPackages) {
                return new HelloWorldBootstrap(lifecycleInjectorBuilder, scanPackages);
            }
        }.build();
    }

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
