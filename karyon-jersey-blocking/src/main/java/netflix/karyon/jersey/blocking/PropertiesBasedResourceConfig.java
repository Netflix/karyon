package netflix.karyon.jersey.blocking;

import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.core.ScanningResourceConfig;
import com.sun.jersey.core.spi.scanning.PackageNamesScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static com.netflix.config.ConfigurationManager.getConfigInstance;

/**
 * An implementation of {@link ResourceConfig} that enables users to define all jersey properties in a property file
 * loaded by karyon via archaius. <br>
 * This supports scanning of classpath (using {@link ScanningResourceConfig}) to discover provider and other resource
 * classes. The scanning of classpath is done lazily, at the first call to {@link #getClasses()} in order to make sure
 * that we do not do scanning too early, even before all properties are loaded.
 *
 * @author Nitesh Kant
 */
public class PropertiesBasedResourceConfig extends ScanningResourceConfig {

    private static final Logger logger = LoggerFactory.getLogger(PropertiesBasedResourceConfig.class);
    private static final String JERSEY_ROOT_PACKAGE = "com.sun.jersey";
    
    private volatile boolean initialized;

    @Override
    public Set<Class<?>> getClasses() {
        initIfRequired();
        return super.getClasses();
    }

    @Override
    public Set<Object> getSingletons() {
        initIfRequired();
        return super.getSingletons();
    }

    @Override
    public Map<String, MediaType> getMediaTypeMappings() {
        initIfRequired();
        return super.getMediaTypeMappings();
    }

    @Override
    public Map<String, String> getLanguageMappings() {
        initIfRequired();
        return super.getLanguageMappings();
    }

    @Override
    public Map<String, Object> getExplicitRootResources() {
        initIfRequired();
        return super.getExplicitRootResources();
    }

    @Override
    public Map<String, Boolean> getFeatures() {
        initIfRequired();
        return super.getFeatures();
    }

    @Override
    public boolean getFeature(String featureName) {
        initIfRequired();
        return super.getFeature(featureName);
    }

    @Override
    public Map<String, Object> getProperties() {
        initIfRequired();
        return super.getProperties();
    }

    @Override
    public Object getProperty(String propertyName) {
        initIfRequired();
        return super.getProperty(propertyName);
    }

    private synchronized void initIfRequired() {
        if (initialized) {
            return;
        }
        initialized = true;
        String pkgNamesStr = getConfigInstance().getString(PackagesResourceConfig.PROPERTY_PACKAGES, null);
        if (null == pkgNamesStr) {
            logger.warn("No property defined with name: " + PackagesResourceConfig.PROPERTY_PACKAGES +
                        ", this means that jersey can not find any of your resource/provider classes.");
        } else {
            String[] pkgNames = getElements(new String[]{pkgNamesStr}, ResourceConfig.COMMON_DELIMITERS);
            logger.info("Packages to scan by jersey {}", Arrays.toString(pkgNames));
            init(new PackageNamesScanner(pkgNames));
        }
        Map<String, Object> jerseyProperties = createPropertiesMap();
        setPropertiesAndFeatures(jerseyProperties);
    }

    private static Map<String, Object> createPropertiesMap() {
        Properties properties = new Properties();
        Iterator<String> iter = getConfigInstance().getKeys(JERSEY_ROOT_PACKAGE);
        while (iter.hasNext()) {
            String key = iter.next();
            properties.setProperty(key, getConfigInstance().getString(key));
        }

        return new TypeSafePropertiesDelegate(properties);
    }

    private static class TypeSafePropertiesDelegate implements Map<String, Object> {

        private final Properties properties;
        // This intends to not make a copy of the properties but just refer to the property name & delegate to the
        // properties instance for values.
        private final Set<Entry<String, Object>> entrySet;

        public TypeSafePropertiesDelegate(Properties properties) {
            this.properties = properties;
            entrySet = new HashSet<Entry<String, Object>>(properties.size());
            for (final String propName : properties.stringPropertyNames()) {
                entrySet.add(new Entry<String, Object>() {
                    @Override
                    public String getKey() {
                        return propName;
                    }

                    @Override
                    public Object getValue() {
                        return TypeSafePropertiesDelegate.this.properties.getProperty(propName);
                    }

                    @Override
                    public Object setValue(Object value) {
                        throw new UnsupportedOperationException("Writes are not supported on jersey features and properties map.");
                    }
                });
            }
        }

        @Override
        public int size() {
            return properties.size();
        }

        @Override
        public boolean isEmpty() {
            return properties.isEmpty();
        }

        @Override
        public boolean containsKey(Object key) {
            return properties.contains(key);
        }

        @Override
        public boolean containsValue(Object value) {
            return properties.containsValue(value);
        }

        @Override
        public Object get(Object key) {
            return properties.getProperty(String.valueOf(key));
        }

        @Override
        public Object put(String key, Object value) {
            throw new UnsupportedOperationException("Writes are not supported on jersey features and properties map.");
        }

        @Override
        public Object remove(Object key) {
            throw new UnsupportedOperationException("Writes are not supported on jersey features and properties map.");
        }

        @Override
        public void putAll(Map<? extends String, ?> m) {
            throw new UnsupportedOperationException("Writes are not supported on jersey features and properties map.");
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException("Writes are not supported on jersey features and properties map.");
        }

        @Override
        public Set<String> keySet() {
            return properties.stringPropertyNames();
        }

        @Override
        public Collection<Object> values() {
            return properties.values();
        }

        @Override
        public Set<Entry<String, Object>> entrySet() {
            return entrySet;
        }
    }
}
