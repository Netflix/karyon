package com.netflix.karyon.log4j.admin;

import com.netflix.governator.auto.annotations.ConditionalOnModule;
import com.netflix.karyon.admin.AbstractAdminModule;
import com.netflix.karyon.admin.AdminModule;

@ConditionalOnModule(AdminModule.class)
public final class Log4jAdminModule extends AbstractAdminModule {
    @Override
    protected void configure() {
        bindAdminResource("log4j").to(Log4JResource.class);
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
