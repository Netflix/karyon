package com.netflix.karyon.admin;

import com.netflix.governator.auto.annotations.ConditionalOnModule;

@ConditionalOnModule(AdminModule.class)
public final class CoreAdminModule extends AbstractAdminModule {
    @Override
    protected void configure() {
        bindAdminResource("guice-keys").to(GuiceKeysAdminResource.class);
        bindAdminResource("env").to(EnvAdminResource.class);
        bindAdminResource("jars").to(JarsAdminResource.class);
        bindAdminResource("log4j").to(Log4jAdminResource.class);
        bindAdminResource("meta").to(MetaAdminResource.class);
    }
    
    @Override
    public boolean equals(Object obj) {
        return CoreAdminModule.class.equals(obj.getClass());
    }

    @Override
    public int hashCode() {
        return CoreAdminModule.class.hashCode();
    }
}
