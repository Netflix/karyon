package netflix.admin;

import java.util.List;
import java.util.Map;

import javax.servlet.Filter;

import com.google.inject.ImplementedBy;
import org.mortbay.jetty.Connector;

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
    List<Connector> additionalConnectors();
    Map<String, Object> getJerseyConfigProperties();
    List<String> homeScriptResources();
}
