package netflix.adminresources;

import com.google.inject.Injector;
import com.netflix.explorers.providers.FreemarkerTemplateProvider;
import com.netflix.explorers.providers.WebApplicationExceptionMapper;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import com.sun.jersey.spi.container.servlet.WebConfig;

import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.servlet.ServletException;

import netflix.admin.AdminFreemarkerTemplateProvider;

/**
 * This class is a minimal simulation of GuiceFilter. Due to the number of
 * statics used in GuiceFilter, there cannot be more than one in an application.
 * The AdminResources app needs minimal features and this class provides those.
 */
class AdminResourcesFilter extends GuiceContainer {
    private volatile String packages;

    @Inject
    AdminResourcesFilter(Injector injector) {
        super(injector);
    }

    /**
     * Set the packages for Jersey to scan for resources
     *
     * @param packages packages to scan
     */
    void setPackages(String packages) {
        this.packages = packages;
    }

    @Override
    protected ResourceConfig getDefaultResourceConfig(Map<String, Object> props,
                                                      WebConfig webConfig) throws ServletException {
        props.put(PackagesResourceConfig.PROPERTY_PACKAGES, packages);
        props.put(ResourceConfig.FEATURE_DISABLE_WADL, "false");
        
        return new PackagesResourceConfig(props) {
            @Override
            public Set<Class<?>> getProviderClasses() {
                Set<Class<?>> providers = super.getProviderClasses();
                // remove conflicting provider if present
                providers.remove(FreemarkerTemplateProvider.class);
                providers.add(AdminFreemarkerTemplateProvider.class);
                providers.add(WebApplicationExceptionMapper.class);
                return providers;
            }
        };
    }
}
