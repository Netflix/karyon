package netflix.admin;

import com.google.inject.ImplementedBy;

import java.util.Map;

@ImplementedBy(DefaultRedirectRules.class)
public interface RedirectRules {
    Map<String, String> getMappings();
}
