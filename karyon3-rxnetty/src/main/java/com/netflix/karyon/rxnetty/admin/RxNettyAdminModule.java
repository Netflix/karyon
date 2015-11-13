package com.netflix.karyon.rxnetty.admin;

import com.google.inject.AbstractModule;
import com.netflix.karyon.admin.AdminModule;
import com.netflix.karyon.conditional.ConditionalOnModule;
import com.netflix.karyon.rxnetty.server.RxNettyModule;

@ConditionalOnModule(value = {AdminModule.class, RxNettyModule.class})
public final class RxNettyAdminModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(RxNettyAdminResource.class);
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
