package com.netflix.karyon.admin;

import com.netflix.governator.auto.annotations.ConditionalOnModule;

@ConditionalOnModule(AdminModule.class)
public final class CoreAdminModule extends AbstractAdminModule {
    @Override
    protected void configure() {
        bindAdminController("guice-keys").to(GuiceKeysAdminController.class);
        bindAdminController("env").to(EnvAdminController.class);
        bindAdminController("jars").to(JarsAdminController.class);
        bindAdminController("log4j").to(Log4jAdminController.class);
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
