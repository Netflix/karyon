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

    private final Archaius archaius;

    @Inject
    public ArchaiusSuite(Archaius archaius) {
        this.archaius = archaius;
    }

    @Override
    public void configure(LifecycleInjectorBuilder builder) {
        builder.withAdditionalBootstrapModules(new BootstrapModule() {

            @Override
            public void configure(BootstrapBinder bootstrapBinder) {
                bootstrapBinder.bind(Archaius.class).toInstance(archaius);
                bootstrapBinder.bind(PropertiesLoader.class).toProvider(archaius.loader());
                ArchaiusConfigurationProvider.Builder builder = ArchaiusConfigurationProvider.builder();
                builder.withOwnershipPolicy(ConfigurationOwnershipPolicies.ownsAll());
                bootstrapBinder.bindConfigurationProvider().toInstance(builder.build());
            }
        });
    }

}
