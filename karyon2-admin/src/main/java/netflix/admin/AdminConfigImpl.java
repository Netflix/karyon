package netflix.admin;

import com.netflix.config.ConfigurationManager;

public class AdminConfigImpl implements AdminContainerConfig {
    public static final String NETFLIX_ADMIN_TEMPLATE_CONTEXT = "netflix.admin.template.context";
    public static final String TEMPLATE_CONTEXT_DEFAULT = "/admin";
    public static final String NETFLIX_ADMIN_RESOURCE_CONTEXT = "netflix.admin.resource.context";
    public static final String RESOURCE_CONTEXT_DEFAULT = "/webadmin";
    public static final String NETFLIX_ADMIN_HEALTH_CHECK_PATH = "netflix.admin.healthcheck.path";
    public static final String HEALTH_CHECK_PATH_DEFAULT = "/healthcheck";
    private final String templateResContext;
    private final String resourceContext;
    private final String healthCheckPath;

    public AdminConfigImpl() {
        templateResContext = ConfigurationManager.getConfigInstance().getString(NETFLIX_ADMIN_TEMPLATE_CONTEXT, TEMPLATE_CONTEXT_DEFAULT);
        resourceContext = ConfigurationManager.getConfigInstance().getString(NETFLIX_ADMIN_RESOURCE_CONTEXT, RESOURCE_CONTEXT_DEFAULT);
        healthCheckPath = ConfigurationManager.getConfigInstance().getString(NETFLIX_ADMIN_HEALTH_CHECK_PATH, HEALTH_CHECK_PATH_DEFAULT);
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
    public String healthCheckPath() {
        return healthCheckPath;
    }
}
