package netflix.admin;

public interface AdminContainerConfig {
    String templateResourceContext();
    String ajaxDataResourceContext();
    String jerseyResourcePkgList();
    String jerseyViewableResourcePkgList();
    boolean shouldScanClassPathForPluginDiscovery();
    int listenPort();
}
