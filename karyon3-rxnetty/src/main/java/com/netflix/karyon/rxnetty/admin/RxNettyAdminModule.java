package com.netflix.karyon.rxnetty.admin;

import com.netflix.governator.auto.annotations.ConditionalOnModule;
import com.netflix.governator.auto.annotations.ConditionalOnProfile;
import com.netflix.karyon.admin.AbstractAdminModule;
import com.netflix.karyon.admin.AdminModule;

@ConditionalOnModule(AdminModule.class)
@ConditionalOnProfile("rxadmin")
public final class RxNettyAdminModule extends AbstractAdminModule {
    @Override
    protected void configure() {
        this.bindAdminResource("rxnetty").to(RxNettyAdminResource.class);
    }
    
    @Override
    public boolean equals(Object obj) {
        return RxNettyAdminModule.class.equals(obj.getClass());
    }

    @Override
    public int hashCode() {
        return RxNettyAdminModule.class.hashCode();
    }
}
