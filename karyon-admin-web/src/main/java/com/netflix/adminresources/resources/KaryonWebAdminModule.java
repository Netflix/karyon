package com.netflix.adminresources.resources;

import com.netflix.adminresources.KaryonAdminModule;
import com.netflix.governator.guice.LifecycleInjectorBuilder;
import com.netflix.governator.guice.LifecycleInjectorBuilderSuite;

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
