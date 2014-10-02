package netflix.admin;

import com.google.common.base.Supplier;
import com.google.inject.Singleton;
import com.netflix.explorers.AbstractExplorerModule;
import com.netflix.explorers.Explorer;
import com.netflix.explorers.ExplorerManager;
import com.netflix.explorers.PropertiesGlobalModelContext;
import com.netflix.explorers.context.GlobalModelContext;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


@Singleton
public class AdminExplorerManager implements ExplorerManager {
    private static final Logger LOG = LoggerFactory.getLogger(AdminExplorerManager.class);

    public static class AdminResourceExplorer extends AbstractExplorerModule {
        public AdminResourceExplorer() {
            super("admin");
        }
    }

    private PropertiesGlobalModelContext propertiesGlobalModelContext;
    private AdminResourceExplorer adminExplorer;

    public AdminExplorerManager() {
        final Properties properties = AdminUtils.loadAdminConsoleProps();
        propertiesGlobalModelContext = new PropertiesGlobalModelContext(properties);
        adminExplorer = new AdminResourceExplorer();
        adminExplorer.initialize();
    }

    @Override
    public void initialize() {
    }

    @Override
    public void shutdown() {
    }

    @Override
    public String getDefaultModule() {
        return null;
    }

    @Override
    public void registerExplorer(Explorer module) {

    }

    @Override
    public void unregisterExplorer(Explorer module) {

    }

    @Override
    public void registerExplorerFromClassName(String className) throws Exception {

    }

    @Override
    public Explorer getExplorer(String name) {
        if (name.equals("admin")) {
            return adminExplorer;
        }
        throw new IllegalArgumentException("AdminExplorerManager called with explorerName = " + name);
    }

    @Override
    public Collection<Explorer> getExplorers() {
        return Collections.EMPTY_LIST;
    }

    @Override
    public GlobalModelContext getGlobalModel() {
        return propertiesGlobalModelContext;
    }

    @Override
    public Configuration getConfiguration() {
        return null;
    }

    @Override
    public void registerExplorersFromClassNames(Set<String> classNames) {

    }

    @Override
    public <T> T getService(Class<T> className) {
        return null;
    }

    @Override
    public <T> void registerService(Class<T> serviceClass, T instance) {

    }

    @Override
    public <T> void registerService(Class<T> serviceClass, Supplier<T> supplier) {

    }

    @Override
    public <T> void registerService(Class<T> serviceClass, Class<? extends T> serviceImplClassName) {

    }

    @Override
    public boolean getHasAuthProvider() {
        return false;
    }
}