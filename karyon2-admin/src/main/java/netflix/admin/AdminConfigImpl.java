package netflix.admin;

import com.netflix.config.ConfigurationManager;

import javax.inject.Singleton;

@Singleton
public class AdminConfigImpl implements AdminContainerConfig {
    public static final String NETFLIX_ADMIN_TEMPLATE_CONTEXT = "netflix.admin.template.context";
    public static final String TEMPLATE_CONTEXT_DEFAULT = "/admin";
    public static final String NETFLIX_ADMIN_RESOURCE_CONTEXT = "netflix.admin.resource.context";
    public static final String RESOURCE_CONTEXT_DEFAULT = "/webadmin";
    private final String templateResContext;
    private final String resourceContext;

    public AdminConfigImpl() {
        templateResContext = ConfigurationManager.getConfigInstance().getString(NETFLIX_ADMIN_TEMPLATE_CONTEXT, TEMPLATE_CONTEXT_DEFAULT);
        resourceContext = ConfigurationManager.getConfigInstance().getString(NETFLIX_ADMIN_RESOURCE_CONTEXT, RESOURCE_CONTEXT_DEFAULT);
    }

    @Override
    public String templateResourceContext() {
        return templateResContext;
    }

    @Override
    public String ajaxDataResourceContext() {
        return resourceContext;
    }
}
