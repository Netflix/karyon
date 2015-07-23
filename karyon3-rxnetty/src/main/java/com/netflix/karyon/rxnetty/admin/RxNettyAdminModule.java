package com.netflix.karyon.rxnetty.admin;

import com.netflix.governator.auto.annotations.ConditionalOnModule;
import com.netflix.karyon.admin.AbstractAdminModule;
import com.netflix.karyon.admin.AdminModule;

@ConditionalOnModule(AdminModule.class)
public final class RxNettyAdminModule extends AbstractAdminModule {
    @Override
    protected void configure() {
        this.bindAdminResource("rxnetty").to(RxNettyAdminResource.class);
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
