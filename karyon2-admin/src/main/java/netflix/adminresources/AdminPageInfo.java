package netflix.adminresources;

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

    // controls if the module should be visible/enabled in admin console
    boolean isEnabled();

    // should it be visible (by rendering page template as defined above)
    boolean isVisible();
}
