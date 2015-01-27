package netflix.karyon.archaius;

import com.netflix.config.ConfigurationManager;
import com.netflix.config.DeploymentContext;
import netflix.karyon.KaryonBootstrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;

/**
 * Default property loading mechanism for archaius. It loads two property files:
 *
 * <ul>
 <li>Any file with name &lt;application_name&gt;.properties</li>
 <li>Any file with name &lt;application_name&gt;-&lt; archaius environment&gt;.properties: The environment name is as
 provided by {@link DeploymentContext#getDeploymentEnvironment()} where the deployment context is
 as configured for archaius and retrieved by {@link ConfigurationManager#getDeploymentContext()}</li>
 </ul>
 *
 * The application name is either provided to this loader or defaults to {@link DeploymentContext#getApplicationId()}
 * where {@link DeploymentContext} is as retrieved by {@link ConfigurationManager#getDeploymentContext()}
 *
 * @author Nitesh Kant
 */
public class DefaultPropertiesLoader implements PropertiesLoader {

    private static final Logger logger = LoggerFactory.getLogger(DefaultPropertiesLoader.class);

    private final String appName;

    public DefaultPropertiesLoader() {
        this.appName = null;
    }

    public DefaultPropertiesLoader(String appName) {
        this.appName = appName;
    }

    @Inject
    DefaultPropertiesLoader(KaryonBootstrap karyonBootstrap) {
        appName = karyonBootstrap.name();
    }

    @Override
    public void load() {
        String appNameToUse = appName;

        if (null == appNameToUse) {
            appNameToUse = ConfigurationManager.getDeploymentContext().getApplicationId();
        }

        try {
            logger.info(String.format("Loading application properties with app id: %s and environment: %s",
                                      appNameToUse,
                                      ConfigurationManager.getDeploymentContext().getDeploymentEnvironment()));
            /**
             * This loads a property file with the name "appName".properties and "appName"-"env".properties, if found.
             */
            ConfigurationManager.loadCascadedPropertiesFromResources(appNameToUse);
        } catch (IOException e) {
            logger.error(String.format(
                    "Failed to load properties for application id: %s and environment: %s. This is ok, if you do not have application level properties.",
                    appNameToUse,
                    ConfigurationManager.getDeploymentContext().getDeploymentEnvironment()), e);
        }
    }
}
