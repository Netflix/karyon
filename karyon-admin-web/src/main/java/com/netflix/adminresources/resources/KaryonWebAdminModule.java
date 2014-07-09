package com.netflix.adminresources.resources;

import com.netflix.adminresources.KaryonAdminModule;

public class KaryonWebAdminModule extends KaryonAdminModule {

    @Override
    protected void configure() {
        super.configure();
        bind(WebAdminComponent.class).asEagerSingleton();
    }
}
