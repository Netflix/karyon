package netflix.adminresources.resources;

import com.netflix.config.ConfigurationManager;
import netflix.adminresources.resources.model.Property;
import org.apache.commons.configuration.AbstractConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class PropertiesHelper {
    private static final Logger LOG = LoggerFactory.getLogger(PropertiesHelper.class);

    public static List<Property> getAllProperties() {
        final AbstractConfiguration configInstance = ConfigurationManager.getConfigInstance();
        List<Property> properties = new ArrayList<Property>();
        Iterator keys = null;
        try {
            keys = configInstance.getKeys();
        } catch (Exception e) {
            LOG.error("Exception fetching all property keys ", e);
        }

        Set<String> maskedResources = MaskedResourceHelper.getMaskedPropertiesSet();

        while (keys.hasNext()) {
            final String key = (String) keys.next();
            try {
                Object value = null;
                // mask the specified properties
                if (maskedResources.contains(key)) {
                    value = MaskedResourceHelper.MASKED_PROPERTY_VALUE;
                } else {
                    value = configInstance.getProperty(key);
                }
                Property property = new Property(key, value.toString(), null);
                properties.add(property);
            } catch (Exception e) {
                LOG.info("Exception fetching property value for key " + key, e);
            }

        }
        return properties;
    }

    public static Map<String, String> buildPropertiesMap(List<Property> properties) {
        Map<String, String> propsMap = new HashMap<>();
        for (Property prop : properties) {
            propsMap.put(prop.getName(), prop.getValue());
        }
        return propsMap;
    }
}

