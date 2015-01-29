package netflix.adminresources;

import com.google.inject.AbstractModule;

public class KaryonAdminModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(AdminResourcesContainer.class).asEagerSingleton();
    }
}
