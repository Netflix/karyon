package netflix.admin;

import com.google.inject.ImplementedBy;

@ImplementedBy(AdminConfigImpl.class)
public interface AdminContainerConfig {
    String templateResourceContext();
    String ajaxDataResourceContext();
    String jerseyResourcePkgList();
    String jerseyViewableResourcePkgList();
    boolean shouldScanClassPathForPluginDiscovery();
    int listenPort();
}
