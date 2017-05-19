package netflix.admin;

import java.util.List;
import java.util.Map;

import javax.servlet.Filter;

import com.google.inject.ImplementedBy;

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
    Map<String, Object> getJerseyConfigProperties();

}
