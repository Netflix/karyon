package netflix.admin;

import com.google.inject.ImplementedBy;

import javax.servlet.Filter;
import java.util.List;

@ImplementedBy(AdminConfigImpl.class)
public interface AdminContainerConfig {
    boolean shouldIsolateResources();
    boolean shouldEnable();
    String templateResourceContext();
    String ajaxDataResourceContext();
    String jerseyResourcePkgList();
    String jerseyViewableResourcePkgList();
    boolean shouldScanClassPathForPluginDiscovery();
    int listenPort();
    List<Filter> additionalFilters();

}
