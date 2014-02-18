package com.netflix.karyon.server.bootstrap;

import com.google.common.base.Preconditions;
import com.netflix.config.ConfigurationManager;
import com.netflix.config.DeploymentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;

/**
 * <h2>Archaius Integration</h2>
 * As such <a href="https://github.com/Netflix/archaius/">Archaius</a> requires minimal integration and works out of the
 * box however, karyon does one additional step of loading the configuration from properties file located in the
 * classpath. The properties loading is done by calling {@link ConfigurationManager#loadCascadedPropertiesFromResources(String)}
 * with the name of the config as returned by {@link DeploymentContext#getApplicationId()} from the
 * deployment context configured in archaius. This {@link ArchaiusInitializer} implementation loads all properties
 * defined in properties file(s) having the names:
 *
 * [application_name].properties
 * [application_name]-[environement].properties
 *
 * The environment above is as retrieved from {@link DeploymentContext#getApplicationId()} from the
 * deployment context configured in archaius. This can be set by a property "archaius.deployment.environment"
 *
 * NOTE: The above property names are valid if the default deployment context is used for archaius.
 *
 * @author Nitesh Kant
 */
public class DefaultArchaiusInitializer implements ArchaiusInitializer {

    protected static final Logger logger = LoggerFactory.getLogger(DefaultArchaiusInitializer.class);

    public static final String ARCHAIUS_DEPLOYMENT_APPLICATION_ID_PROP_NAME = "archaius.deployment.applicationId";
    public static final String ARCHAIUS_DEPLOYMENT_ENVIRONMENT_PROP_NAME = "archaius.deployment.environment";

    public DefaultArchaiusInitializer(String applicationName, @Nullable String environment) {
        Preconditions.checkNotNull(applicationName, "Application name can not be null.");
        System.setProperty(ARCHAIUS_DEPLOYMENT_APPLICATION_ID_PROP_NAME, applicationName);
        if (null != environment) {
            System.setProperty(ARCHAIUS_DEPLOYMENT_ENVIRONMENT_PROP_NAME, environment);
        }
    }

    @Override
    public void initialize() {
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
            logger.warn("Application identifier not defined, skipping application level properties loading. " +
                        "You must set a property " + ARCHAIUS_DEPLOYMENT_APPLICATION_ID_PROP_NAME +
                        " to be able to load application level properties.");
        }
    }
}
