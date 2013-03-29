package com.netflix.karyon.server;

import com.netflix.config.ConfigurationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.EnumSet;

/**
 * @author Nitesh Kant
 */
class ArchaiusIntegrationInterceptor implements InitializationPhaseInterceptor {

    protected static final Logger logger = LoggerFactory.getLogger(ArchaiusIntegrationInterceptor.class);

    @Override
    public void onPhase(Phase phase) {
        String appId = ConfigurationManager.getDeploymentContext().getApplicationId();

        // Loading properties via archaius.
        if (null != appId) {
            try {
                logger.info(String.format("Loading application properties with app id: %s and environment: %s", appId,
                        ConfigurationManager.getDeploymentContext().getDeploymentEnvironment()));
                ConfigurationManager.loadCascadedPropertiesFromResources(appId);
            } catch (IOException e) {
                logger.error(String.format(
                        "Failed to load properties for application id: %s and environment: %s. This is ok, if you do not have application level properties.",
                        appId,
                        ConfigurationManager.getDeploymentContext().getDeploymentEnvironment()), e);
            }
        } else {
            logger.warn(
                    "Application identifier not defined, skipping application level properties loading. You must set a property 'archaius.deployment.applicationId' to be able to load application level properties.");
        }
    }

    @Override
    public EnumSet<Phase> interestedIn() {
        return EnumSet.of(Phase.OnCreate);
    }
}
