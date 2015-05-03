package netflix.admin;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

@Singleton
public class DefaultRedirectRules implements RedirectRules {

    private AdminContainerConfig adminContainerConfig;

    @Inject
    public DefaultRedirectRules(AdminContainerConfig adminContainerConfig) {
        this.adminContainerConfig = adminContainerConfig;
    }

    @Override
    public Map<String, String> getMappings() {
        Map<String, String> urlRedirects = new HashMap<>();
        urlRedirects.put("/", adminContainerConfig.templateResourceContext());
        return urlRedirects;
    }

    @Override
    public String getRedirect(HttpServletRequest httpServletRequest) {
        return httpServletRequest.getRequestURI();
    }
}
