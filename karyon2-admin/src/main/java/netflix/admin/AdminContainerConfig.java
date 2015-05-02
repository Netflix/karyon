package netflix.admin;

import com.google.inject.ImplementedBy;

@ImplementedBy(AdminConfigImpl.class)
public interface AdminContainerConfig {
    boolean shouldEnable();
    String templateResourceContext();
    String ajaxDataResourceContext();
    String jerseyResourcePkgList();
    String jerseyViewableResourcePkgList();
    boolean shouldScanClassPathForPluginDiscovery();
    int listenPort();
}
