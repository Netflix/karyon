package netflix.admin;

import com.netflix.config.ConfigurationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import javax.servlet.Filter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Singleton
public class AdminConfigImpl implements AdminContainerConfig {
    private static final Logger logger = LoggerFactory.getLogger(AdminConfigImpl.class);

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
    public List<Filter> additionalFilters() {
        String rootContextFilters = ConfigurationManager.getConfigInstance().getString(NETFLIX_ADMIN_CTX_FILTERS, DEFAULT_CONTEXT_FILTERS);

        if (rootContextFilters.isEmpty()) {
            return Collections.emptyList();
        }

        List<Filter> filters = new ArrayList<>();
        final String[] filterClasses = rootContextFilters.split(",");
        for (String filterClass : filterClasses) {
            try {
                getClass().getClassLoader();
                final Class<?> filterCls = Class.forName(filterClass, false, getClass().getClassLoader());
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
