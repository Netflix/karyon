package netflix.adminresources;

import javax.inject.Named;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

public class KaryonAdminModule extends AbstractModule {

    public static final String ADMIN_RESOURCES_SERVER_PORT = "adminResourcesServerPort";

    @Override
    protected void configure() {
        bind(AdminResourcesContainer.class).asEagerSingleton();
    }

    @Provides
    @Named(ADMIN_RESOURCES_SERVER_PORT)
    public int adminListenPort(AdminResourcesContainer adminResourcesContainer) {
        return adminResourcesContainer.getServerPort();
    }
}
