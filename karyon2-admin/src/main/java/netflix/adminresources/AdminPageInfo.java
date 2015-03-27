package netflix.adminresources;

import com.google.inject.Module;

import java.util.List;
import java.util.Map;

public interface AdminPageInfo {
    // id of the new page
    String getPageId();

    // title as shown in tab UI
    String getName();

    // freemarker template path
    String getPageTemplate();

    // exports additional bindings needed by the plugin
    Map<String, Object> getDataModel();

    // additional jersey resource package list if needed
    String getJerseyResourcePackageList();

    // Guice modules that need to be added to the injector
    List<Module> getGuiceModules();

    // controls if the module should be visible/enabled in admin console
    boolean isEnabled();

    // should it be visible (by rendering page template as defined above)
    boolean isVisible();
}
