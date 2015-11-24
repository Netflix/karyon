package com.netflix.karyon.admin;

import com.google.inject.AbstractModule;
import com.google.inject.grapher.NameFactory;
import com.google.inject.grapher.ShortNameFactory;
import com.google.inject.grapher.graphviz.PortIdFactory;
import com.google.inject.grapher.graphviz.PortIdFactoryImpl;

public final class CoreAdminModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(GuiceKeysAdminResource.class);
        bind(DIGraphResource.class);
        bind(EnvAdminResource.class);
        bind(JarsAdminResource.class);
        bind(HealthCheckResource.class);
        bind(GuiceLifecycleResource.class);
        bind(DIProvisionResource.class);
        bind(ThreadsAdminResource.class);
        bind(SystemInfoResource.class);
        
        // These are needed in DIGraphResource
        bind(NameFactory.class).to(ShortNameFactory.class);
        bind(PortIdFactory.class).to(PortIdFactoryImpl.class);
    }
    
    @Override
    public boolean equals(Object obj) {
        return getClass().equals(obj.getClass());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
