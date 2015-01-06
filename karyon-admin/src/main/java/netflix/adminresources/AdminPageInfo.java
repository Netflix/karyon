package netflix.adminresources;

public interface AdminPageInfo {
    // id of the new page
    String getPageId();

    // title as shown in tab UI
    String getName();

    // freemarker template path
    String getPageTemplate();

    // additional jersey resource package list if needed
    String getJerseyResourcePackageList();

    // controls if the module should be visible/enabled in admin console
    boolean isEnabled();

}
