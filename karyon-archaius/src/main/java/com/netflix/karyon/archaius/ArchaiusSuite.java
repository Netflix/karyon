package com.netflix.karyon.archaius;

import java.io.IOException;
import java.util.Iterator;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.ProvisionException;
import com.netflix.config.ConfigurationManager;
import com.netflix.governator.configuration.ArchaiusConfigurationProvider;
import com.netflix.governator.configuration.ConfigurationOwnershipPolicies;
import com.netflix.governator.guice.BootstrapBinder;
import com.netflix.governator.guice.BootstrapModule;
import com.netflix.governator.guice.LifecycleInjectorBuilder;
import com.netflix.governator.guice.LifecycleInjectorBuilderSuite;
import com.netflix.karyon.KaryonBootstrap;

/**
 * A guice module that defines all bindings required by karyon. Applications must use this to bootstrap karyon.
 *
 * @author Nitesh Kant
 * @author elandau
 */
@Singleton
public class ArchaiusSuite implements LifecycleInjectorBuilderSuite {
    private static final Logger logger = LoggerFactory.getLogger(ArchaiusSuite.class);
    
    public static enum Action {
        include,
        exclude
    }
    
    private final KaryonBootstrap karyonBootstrap;
    private final ArchaiusBootstrap archaiusBootstrap;
    private final Injector injector;
    
    @Inject
    public ArchaiusSuite(Injector injector, ArchaiusBootstrap archaiusBootstrap, KaryonBootstrap karyonBootstrap) {
        this.karyonBootstrap = karyonBootstrap;
        this.archaiusBootstrap = archaiusBootstrap;
        this.injector = injector;
    }

    @Override
    public void configure(LifecycleInjectorBuilder builder) {
        // First, load the properties from archaius
        try {
            logger.info(String.format("Loading application properties with app id: %s and environment: %s",
                                      karyonBootstrap.name(),
                                      ConfigurationManager.getDeploymentContext().getDeploymentEnvironment()));
            ConfigurationManager.loadCascadedPropertiesFromResources(karyonBootstrap.name());
        } catch (IOException e) {
            logger.error(
                    "Failed to load properties for application id: {} and environment: {}. This is ok, if you do not have application level properties.",
                    karyonBootstrap.name(),
                    ConfigurationManager.getDeploymentContext().getDeploymentEnvironment(), 
                    e);
        }

        // Load overrides
        for (Class<? extends PropertiesLoader> overrides : archaiusBootstrap.overrides()) {
            injector.getInstance(overrides).load();
        }
        
        // Next, load any dynamic modules from the properties
        if (archaiusBootstrap.enableModuleLoading()) {
            Configuration subset = ConfigurationManager.getConfigInstance().subset(archaiusBootstrap.prefix());
            Iterator<String> iter = subset.getKeys();
            while (iter.hasNext()) {
                String key = iter.next();
                Action action  = Action.valueOf(subset.getString(key));
                switch (action) {
                case include:
                    logger.debug("Including module {}", key);
                    try {
                        @SuppressWarnings("unchecked")
                        Class<? extends Module> moduleClass = (Class<? extends Module>) Class.forName(key);
                        builder.withAdditionalModuleClasses(moduleClass);
                    }
                    catch (Exception e) {
                        throw new ProvisionException("Unable to load module '" + key + "'", e);
                    }
                    break;
                case exclude:
                    logger.debug("Excluding module {}", key);
                    try {
                        @SuppressWarnings("unchecked")
                        Class<? extends Module> moduleClass = (Class<? extends Module>) Class.forName(key);
                        builder.withoutModuleClass(moduleClass);
                    }
                    catch (Exception e) {
                        logger.error("Can't find excluded module '{}'.  This is ok because the module would be excluded anyway", e);
                    }
                    break;
                }
            }
        }
        
        // Finally add the BootstrapModule bindings to Archaius
        builder.withAdditionalBootstrapModules(new BootstrapModule() {
            @Override
            public void configure(BootstrapBinder bootstrapBinder) {
                bootstrapBinder
                    .bindConfigurationProvider()
                    .toInstance(ArchaiusConfigurationProvider.builder()
                            .withOwnershipPolicy(ConfigurationOwnershipPolicies.ownsAll())
                            .build());
            }
        });
        

    }
}
