package com.netflix.karyon.rxnetty.admin;

import com.google.inject.AbstractModule;

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
