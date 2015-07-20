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
        return HttpServerModule.class.equals(obj.getClass());
    }

    @Override
    public int hashCode() {
        return HttpServerModule.class.hashCode();
    }
}
