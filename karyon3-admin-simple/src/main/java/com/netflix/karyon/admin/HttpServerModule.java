package com.netflix.karyon.admin;


public final class HttpServerModule extends AbstractAdminModule {
    @Override
    protected void configure() {
        bind(SimpleHttpServerStarter.class).asEagerSingleton();
        bind(HttpServerRegistry.class).to(GuiceHttpServerRegistry.class);
        bindAdminResource("http").to(HttpServerAdminResource.class);
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
