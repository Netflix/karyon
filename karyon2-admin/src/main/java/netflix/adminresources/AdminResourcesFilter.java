package netflix.adminresources;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.servlet.ServletException;

import com.google.inject.Injector;
import com.netflix.explorers.providers.FreemarkerTemplateProvider;
import com.netflix.explorers.providers.WebApplicationExceptionMapper;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import com.sun.jersey.spi.container.servlet.WebConfig;

import netflix.admin.AdminFreemarkerTemplateProvider;

/**
 * This class is a minimal simulation of GuiceFilter. Due to the number of
 * statics used in GuiceFilter, there cannot be more than one in an application.
 * The AdminResources app needs minimal features and this class provides those.
 */
class AdminResourcesFilter extends GuiceContainer {
    private Map<String, Object> props = Collections.emptyMap();

    @Inject
    AdminResourcesFilter(Injector injector) {
        super(injector);
    }

    @Override
    protected ResourceConfig getDefaultResourceConfig(Map<String, Object> props,
                                                      WebConfig webConfig) throws ServletException {
        HashMap<String, Object> mergedProps = new HashMap<>(props);
        mergedProps.putAll(this.props);
        return new PackagesResourceConfig(mergedProps) {
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

    public void setProperties(Map<String, Object> props) {
        this.props = new HashMap<>(props);
    }
}
