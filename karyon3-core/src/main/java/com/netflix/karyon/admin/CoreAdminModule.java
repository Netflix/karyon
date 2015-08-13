package com.netflix.karyon.admin;

import com.google.inject.grapher.NameFactory;
import com.google.inject.grapher.ShortNameFactory;
import com.google.inject.grapher.graphviz.PortIdFactory;
import com.google.inject.grapher.graphviz.PortIdFactoryImpl;
import com.netflix.governator.auto.annotations.ConditionalOnModule;

@ConditionalOnModule(AdminModule.class)
public final class CoreAdminModule extends AbstractAdminModule {
    @Override
    protected void configure() {
        bindAdminResource("guice-keys").to(GuiceKeysAdminResource.class);
        bindAdminResource("di-graph").to(DIGraphResource.class);
        bindAdminResource("env").to(EnvAdminResource.class);
        bindAdminResource("jars").to(JarsAdminResource.class);
        bindAdminResource("meta").to(MetaAdminResource.class);
        bindAdminResource("health").to(HealthCheckResource.class);
        bindAdminResource("lifecycle").to(ApplicationLifecycleResource.class);
        bindAdminResource("di-provision").to(DIProvisionResource.class);
        
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
