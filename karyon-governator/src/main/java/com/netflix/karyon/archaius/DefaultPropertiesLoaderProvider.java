package com.netflix.karyon.archaius;

import com.google.inject.Provider;
import com.netflix.config.ConfigurationManager;
import com.netflix.karyon.KaryonBootstrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author Nitesh Kant
 */
public class DefaultPropertiesLoaderProvider implements Provider<PropertiesLoader> {

    private static final Logger logger = LoggerFactory.getLogger(DefaultPropertiesLoaderProvider.class);
    private String appName;

    public DefaultPropertiesLoaderProvider(KaryonBootstrap karyonBootstrap) {
        this.appName = karyonBootstrap.name();
    }

    @Override
    public PropertiesLoader get() {
        return new PropertiesLoader() {
            @Override
            public void load() {
                try {
                    logger.info(String.format("Loading application properties with app id: %s and environment: %s",
                                              appName,
                                              ConfigurationManager.getDeploymentContext().getDeploymentEnvironment()));
                    ConfigurationManager.loadCascadedPropertiesFromResources(appName);
                } catch (IOException e) {
                    logger.error(String.format(
                            "Failed to load properties for application id: %s and environment: %s. This is ok, if you do not have application level properties.",
                            appName,
                            ConfigurationManager.getDeploymentContext().getDeploymentEnvironment()), e);
                }
            }
        };
    }
}
