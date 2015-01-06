package netflix.adminresources.resources;

import com.netflix.governator.guice.BootstrapModule;
import netflix.adminresources.KaryonAdminModule;
import netflix.karyon.Karyon;

public class KaryonWebAdminModule extends KaryonAdminModule {

    @Override
    protected void configure() {
        super.configure();
        bind(WebAdminComponent.class).asEagerSingleton();
    }

    public static BootstrapModule asBootstrapModule() {
        return Karyon.toBootstrapModule(KaryonWebAdminModule.class);
    }
}
