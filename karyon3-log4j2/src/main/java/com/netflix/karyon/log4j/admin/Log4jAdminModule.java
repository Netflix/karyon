package com.netflix.karyon.log4j.admin;

import com.netflix.karyon.admin.AbstractAdminModule;
import com.netflix.karyon.admin.AdminModule;
import com.netflix.karyon.conditional.ConditionalOnModule;

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
