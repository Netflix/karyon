package netflix.admin;

import com.google.inject.ImplementedBy;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@ImplementedBy(DefaultRedirectRules.class)
public interface RedirectRules {
    Map<String, String> getMappings();
    String getRedirect(HttpServletRequest httpServletRequest);
}
