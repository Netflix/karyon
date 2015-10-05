package netflix.admin;

import com.netflix.config.ConfigurationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import javax.servlet.Filter;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class AdminConfigImpl implements AdminContainerConfig {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final String NETFLIX_ADMIN_TEMPLATE_CONTEXT = "netflix.admin.template.context";
    public static final String TEMPLATE_CONTEXT_DEFAULT = "/admin";

    public static final String NETFLIX_ADMIN_RESOURCE_CONTEXT = "netflix.admin.resource.context";
    public static final String RESOURCE_CONTEXT_DEFAULT = "/webadmin";

    private static final String JERSEY_CORE_RESOURCES = "netflix.platform.admin.resources.core.packages";
    public static final String JERSEY_CORE_RESOURCES_DEFAULT = "netflix.adminresources;com.netflix.explorers.resources;com.netflix.explorers.providers";

    private static final String JERSEY_VIEWABLE_RESOURCES = "netflix.platform.admin.resources.viewable.packages";
    public static final String JERSEY_VIEWABLE_RESOURCES_DEFAULT = "netflix.admin;netflix.adminresources.pages;com.netflix.explorers.resources";

    public static final String CONTAINER_LISTEN_PORT = "netflix.platform.admin.resources.port";
    public static final int LISTEN_PORT_DEFAULT = 8077;

    public static final String SERVER_ENABLE_PROP_NAME = "netflix.platform.admin.resources.enable";
    public static final boolean SERVER_ENABLE_DEFAULT = true;

    public static final String NETFLIX_ADMIN_RESOURCES_ISOLATE = "netflix.admin.resources.isolate";
    public static final boolean ISOLATE_RESOURCES_DEFAULT = false;

    public static final String NETFLIX_ADMIN_CTX_FILTERS = "netflix.admin.additional.filters";
    public static final String DEFAULT_CONTEXT_FILTERS = "";


    private final String templateResContext;
    private final String resourceContext;
    private final String coreJerseyResources;
    private final String viewableResources;
    private final int listenPort;
    private final boolean isEnabled;
    private final boolean isResourcesIsolated;
    private final String rootContextFilters;

    public AdminConfigImpl() {
        isEnabled = ConfigurationManager.getConfigInstance().getBoolean(SERVER_ENABLE_PROP_NAME, SERVER_ENABLE_DEFAULT);
        templateResContext = ConfigurationManager.getConfigInstance().getString(NETFLIX_ADMIN_TEMPLATE_CONTEXT, TEMPLATE_CONTEXT_DEFAULT);
        resourceContext = ConfigurationManager.getConfigInstance().getString(NETFLIX_ADMIN_RESOURCE_CONTEXT, RESOURCE_CONTEXT_DEFAULT);
        coreJerseyResources = ConfigurationManager.getConfigInstance().getString(JERSEY_CORE_RESOURCES, JERSEY_CORE_RESOURCES_DEFAULT);
        viewableResources = ConfigurationManager.getConfigInstance().getString(JERSEY_VIEWABLE_RESOURCES, JERSEY_VIEWABLE_RESOURCES_DEFAULT);
        listenPort = ConfigurationManager.getConfigInstance().getInt(CONTAINER_LISTEN_PORT, LISTEN_PORT_DEFAULT);
        isResourcesIsolated = ConfigurationManager.getConfigInstance().getBoolean(NETFLIX_ADMIN_RESOURCES_ISOLATE, ISOLATE_RESOURCES_DEFAULT);
        rootContextFilters = ConfigurationManager.getConfigInstance().getString(NETFLIX_ADMIN_CTX_FILTERS, DEFAULT_CONTEXT_FILTERS);
    }

    @Override
    public boolean shouldIsolateResources() {
        return isResourcesIsolated;
    }

    @Override
    public boolean shouldEnable() {
        return isEnabled;
    }

    @Override
    public String templateResourceContext() {
        return templateResContext;
    }

    @Override
    public String ajaxDataResourceContext() {
        return resourceContext;
    }

    @Override
    public String jerseyResourcePkgList() {
        return coreJerseyResources;
    }

    @Override
    public String jerseyViewableResourcePkgList() {
        return viewableResources;
    }

    @Override
    public boolean shouldScanClassPathForPluginDiscovery() {
        return true;
    }

    @Override
    public int listenPort() {
        return listenPort;
    }

    @Override
    public List<Filter> additionalFilters() {
        if (rootContextFilters.isEmpty()) {
            new ArrayList<>();
        }

        List<Filter> filters = new ArrayList<>();
        final String[] filterClasses = rootContextFilters.split(",");
        for (String filterClass : filterClasses) {
            try {
                final Class<?> filterCls = Class.forName(filterClass);
                if (Filter.class.isAssignableFrom(filterCls)) {
                    Filter filter = (Filter) filterCls.newInstance();
                    filters.add(filter);
                }
            } catch (InstantiationException | IllegalAccessException e) {
                logger.warn("Filter class can not be instantiated " + filterClass);
            } catch (ClassNotFoundException e) {
                logger.warn("Filter class not found " + filterClass);
            }
        }
        return filters;
    }
}
