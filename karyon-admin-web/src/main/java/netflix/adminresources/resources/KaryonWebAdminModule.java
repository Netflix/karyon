package netflix.adminresources.resources;

import com.netflix.governator.guice.LifecycleInjectorBuilder;
import com.netflix.governator.guice.LifecycleInjectorBuilderSuite;
import netflix.adminresources.KaryonAdminModule;

public class KaryonWebAdminModule extends KaryonAdminModule {

    @Override
    protected void configure() {
        super.configure();
        bind(WebAdminComponent.class).asEagerSingleton();
    }

    public static LifecycleInjectorBuilderSuite asSuite() {
        return new LifecycleInjectorBuilderSuite() {
            @Override
            public void configure(LifecycleInjectorBuilder builder) {
                builder.withAdditionalModules(new KaryonWebAdminModule());
            }
        };
    }
}
