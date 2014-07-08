package com.netflix.karyon.archaius;

import com.netflix.governator.configuration.ArchaiusConfigurationProvider;
import com.netflix.governator.configuration.ConfigurationOwnershipPolicies;
import com.netflix.governator.guice.BootstrapBinder;
import com.netflix.governator.guice.BootstrapModule;
import com.netflix.governator.guice.LifecycleInjectorBuilder;
import com.netflix.governator.guice.LifecycleInjectorBuilderSuite;

import javax.inject.Inject;

/**
 * A guice module that defines all bindings required by karyon. Applications must use this to bootstrap karyon.
 *
 * @author Nitesh Kant
 */
public class ArchaiusSuite implements LifecycleInjectorBuilderSuite {

    private final ArchaiusBootstrap archaiusBootstrap;

    @Inject
    public ArchaiusSuite(ArchaiusBootstrap archaiusBootstrap) {
        this.archaiusBootstrap = archaiusBootstrap;
    }

    @Override
    public void configure(LifecycleInjectorBuilder builder) {
        builder.withAdditionalBootstrapModules(new BootstrapModule() {

            @Override
            public void configure(BootstrapBinder bootstrapBinder) {
                bootstrapBinder.bind(ArchaiusBootstrap.class).toInstance(archaiusBootstrap);
                bootstrapBinder.bind(PropertiesLoader.class).toProvider(archaiusBootstrap.loader());
                bootstrapBinder.bind(PropertiesInitializer.class).asEagerSingleton();
                ArchaiusConfigurationProvider.Builder builder = ArchaiusConfigurationProvider.builder();
                builder.withOwnershipPolicy(ConfigurationOwnershipPolicies.ownsAll());
                bootstrapBinder.bindConfigurationProvider().toInstance(builder.build());
            }
        });
    }

    public static class PropertiesInitializer {

        @Inject
        public PropertiesInitializer(PropertiesLoader loader) {
            loader.load();
        }
    }

}
