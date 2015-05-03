package netflix.admin;

import com.google.common.base.Supplier;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.netflix.explorers.AbstractExplorerModule;
import com.netflix.explorers.Explorer;
import com.netflix.explorers.ExplorerManager;
import com.netflix.explorers.PropertiesGlobalModelContext;
import com.netflix.explorers.context.GlobalModelContext;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;
import java.util.Set;


@Singleton
public class AdminExplorerManager implements ExplorerManager {
    private static final Logger LOG = LoggerFactory.getLogger(AdminExplorerManager.class);
    public static final String ADMIN_EXPLORER_NAME = "baseserver";

    public static class AdminResourceExplorer extends AbstractExplorerModule {
        public AdminResourceExplorer() {
            super(ADMIN_EXPLORER_NAME);
        }
    }

    private PropertiesGlobalModelContext propertiesGlobalModelContext;
    private AdminResourceExplorer adminExplorer;

    @Inject
    private GlobalModelContextOverride globalModelContextOverride;

    @PostConstruct
    @Override
    public void initialize() {
        Properties properties = AdminUtils.loadAdminConsoleProps();
        if (globalModelContextOverride != null) {
            properties = globalModelContextOverride.overrideProperties(properties);
        }
        propertiesGlobalModelContext = new PropertiesGlobalModelContext(properties);
        adminExplorer = new AdminResourceExplorer();
        adminExplorer.initialize();
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
        if (name.equals(ADMIN_EXPLORER_NAME)) {
            return adminExplorer;
        }
        throw new IllegalArgumentException("AdminExplorerManager called with explorerName = " + name);
    }

    @Override
    public Collection<Explorer> getExplorers() {
        return Collections.emptyList();
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