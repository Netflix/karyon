package netflix.admin;

import com.google.inject.Injector;
import com.netflix.config.ConfigurationManager;
import com.netflix.config.util.ConfigurationUtils;
import org.mortbay.jetty.Connector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.Filter;
import java.util.*;
import java.util.Map.Entry;

@Singleton
public class AdminConfigImpl implements AdminContainerConfig {
    private static final Logger logger = LoggerFactory.getLogger(AdminConfigImpl.class);

    public static final String ADMIN_PREFIX = "netflix.admin.";
    
    public static final String NETFLIX_ADMIN_TEMPLATE_CONTEXT = ADMIN_PREFIX + "template.context";
    public static final String TEMPLATE_CONTEXT_DEFAULT = "/admin";

    public static final String NETFLIX_ADMIN_RESOURCE_CONTEXT = ADMIN_PREFIX + "resource.context";
    public static final String RESOURCE_CONTEXT_DEFAULT = "/webadmin";

    private static final String JERSEY_CORE_RESOURCES = "netflix.platform.admin.resources.core.packages";
    public static final String JERSEY_CORE_RESOURCES_DEFAULT = "netflix.adminresources;com.netflix.explorers.resources;com.netflix.explorers.providers";

    private static final String JERSEY_VIEWABLE_RESOURCES = "netflix.platform.admin.resources.viewable.packages";
    public static final String JERSEY_VIEWABLE_RESOURCES_DEFAULT = "netflix.admin;netflix.adminresources.pages;com.netflix.explorers.resources";

    public static final String CONTAINER_LISTEN_PORT = "netflix.platform.admin.resources.port";
    public static final int LISTEN_PORT_DEFAULT = 8077;

    public static final String SERVER_ENABLE_PROP_NAME = "netflix.platform.admin.resources.enable";
    public static final boolean SERVER_ENABLE_DEFAULT = true;

    public static final String NETFLIX_ADMIN_RESOURCES_ISOLATE = ADMIN_PREFIX + "resources.isolate";
    public static final boolean ISOLATE_RESOURCES_DEFAULT = false;

    public static final String NETFLIX_ADMIN_CTX_FILTERS = ADMIN_PREFIX + "additional.filters";
    public static final String DEFAULT_CONTEXT_FILTERS = "";

    public static final String NETFLIX_ADMIN_CTX_CONNECTORS = ADMIN_PREFIX + "additional.connectors";
    public static final String DEFAULT_CONTEXT_CONNECTORS = "";

    private static final String JERSEY_PROPERTY_PREFIX = "com.sun.jersey.config";
    private static final String ADMIN_JERSEY_PROPERTY_PREFIX = ADMIN_PREFIX + JERSEY_PROPERTY_PREFIX;
    
    private final Injector injector;

    public AdminConfigImpl() {
        this(null);
    }
    
    @Inject
    public AdminConfigImpl(Injector injector) {
        this.injector = injector;
    }
    
    @Override
    public boolean shouldIsolateResources() {
        return ConfigurationManager.getConfigInstance().getBoolean(NETFLIX_ADMIN_RESOURCES_ISOLATE, ISOLATE_RESOURCES_DEFAULT);
    }

    @Override
    public boolean shouldEnable() {
        return ConfigurationManager.getConfigInstance().getBoolean(SERVER_ENABLE_PROP_NAME, SERVER_ENABLE_DEFAULT);
    }

    @Override
    public String templateResourceContext() {
        return ConfigurationManager.getConfigInstance().getString(NETFLIX_ADMIN_TEMPLATE_CONTEXT, TEMPLATE_CONTEXT_DEFAULT);
    }

    @Override
    public String ajaxDataResourceContext() {
        return ConfigurationManager.getConfigInstance().getString(NETFLIX_ADMIN_RESOURCE_CONTEXT, RESOURCE_CONTEXT_DEFAULT);
    }

    @Override
    public String jerseyResourcePkgList() {
        return ConfigurationManager.getConfigInstance().getString(JERSEY_CORE_RESOURCES, JERSEY_CORE_RESOURCES_DEFAULT);
    }

    @Override
    public String jerseyViewableResourcePkgList() {
        return ConfigurationManager.getConfigInstance().getString(JERSEY_VIEWABLE_RESOURCES, JERSEY_VIEWABLE_RESOURCES_DEFAULT);
    }

    @Override
    public boolean shouldScanClassPathForPluginDiscovery() {
        return true;
    }

    @Override
    public int listenPort() {
        return ConfigurationManager.getConfigInstance().getInt(CONTAINER_LISTEN_PORT, LISTEN_PORT_DEFAULT);
    }

    @Override
    public Map<String, Object> getJerseyConfigProperties() {
        Map<String, Object> result = new HashMap<>();
        Properties props = ConfigurationUtils.getProperties(ConfigurationManager.getConfigInstance().subset(ADMIN_JERSEY_PROPERTY_PREFIX));
        for (Entry<Object, Object> prop : props.entrySet()) {
            result.put(JERSEY_PROPERTY_PREFIX + "." + prop.getKey().toString(), prop.getValue().toString());
        }
        return result;
    }

    @Override
    public List<String> homeScriptResources() {
        return Collections.emptyList();
    }
    
    @Override
    public List<Filter> additionalFilters() {
        return getInstancesFromClassList(
                ConfigurationManager.getConfigInstance().getString(NETFLIX_ADMIN_CTX_FILTERS, DEFAULT_CONTEXT_FILTERS),
                Filter.class);
    }

    @Override
    public List<Connector> additionalConnectors() {
        return getInstancesFromClassList(
                ConfigurationManager.getConfigInstance().getString(NETFLIX_ADMIN_CTX_CONNECTORS, DEFAULT_CONTEXT_CONNECTORS),
                Connector.class);
    }

    private <T> List<T> getInstancesFromClassList(String classList, Class<T> clazz) {
        if (classList.isEmpty()) {
            return Collections.emptyList();
        }

        List<T> instances = new ArrayList<>();
        final String[] classNames = classList.split(",");
        for (String className : classNames) {
            try {
                final Class<?> implClass = Class.forName(className, false, getClass().getClassLoader());
                if (clazz.isAssignableFrom(implClass)) {
                    instances.add(clazz.cast(injector == null ? implClass.newInstance() : injector.getInstance(implClass)));
                }
            } catch (InstantiationException | IllegalAccessException e) {
                logger.warn("Class can not be instantiated " + className);
            } catch (ClassNotFoundException e) {
                logger.warn("Class not found " + className);
            }
        }
        return instances;
    }
}
