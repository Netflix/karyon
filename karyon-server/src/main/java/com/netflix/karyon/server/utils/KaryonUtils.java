package com.netflix.karyon.server.utils;

import com.netflix.karyon.spi.PropertyNames;
import org.apache.commons.configuration.AbstractConfiguration;

/**
 * Utility methods for karyon
 *
 * @author Nitesh Kant (nkant@netflix.com)
 */
public class KaryonUtils {

    /**
     * Karyon provides a few core components like eureka which can be enabled/disabled using a property of the form:
     * {@link com.netflix.karyon.spi.PropertyNames#KARYON_PROPERTIES_PREFIX}.[component_name].disable with the value
     * set to true for disabling. By default all core components are enabled.
     *
     * @param componentName Name of the component for which the flag is to be retrieved.
     *
     * @return <code>true</code> if the component is enabled.
     */
    public static boolean isCoreComponentEnabled(AbstractConfiguration archaiusConfig, String componentName) {
        return !archaiusConfig.getBoolean(getPropname(componentName), false);
    }

    private static String getPropname(String componentName) {
        StringBuilder builder = new StringBuilder();
        builder.append(PropertyNames.KARYON_PROPERTIES_PREFIX);
        builder.append(componentName);
        builder.append(".disable");
        return builder.toString();
    }

}
