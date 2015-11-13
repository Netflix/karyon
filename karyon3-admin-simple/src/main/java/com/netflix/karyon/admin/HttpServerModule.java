package com.netflix.karyon.admin;

import com.google.inject.AbstractModule;


public final class HttpServerModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(HttpServerRegistry.class).to(GuiceHttpServerRegistry.class);
        bind(HttpServerAdminResource.class);
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
