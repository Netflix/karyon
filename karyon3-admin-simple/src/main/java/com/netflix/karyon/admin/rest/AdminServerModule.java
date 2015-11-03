package com.netflix.karyon.admin.rest;

import javax.inject.Singleton;

import com.google.inject.Provides;
import com.google.inject.multibindings.MapBinder;
import com.netflix.archaius.ConfigProxyFactory;
import com.netflix.governator.DefaultModule;
import com.netflix.karyon.admin.AdminModule;
import com.netflix.karyon.admin.AdminServer;
import com.netflix.karyon.admin.HttpServerConfig;
import com.netflix.karyon.admin.HttpServerModule;
import com.netflix.karyon.admin.SimpleHttpServer;
import com.sun.net.httpserver.HttpHandler;

public final class AdminServerModule extends DefaultModule {
    @Override
    protected void configure() {
        install(new AdminModule());
        install(new HttpServerModule());
        
        bind(SimpleHttpServer.class).annotatedWith(AdminServer.class).to(AdminRestHttpServer.class).asEagerSingleton();

        MapBinder.newMapBinder(binder(), String.class, HttpHandler.class, AdminServer.class);
    }
    
    // This binds our admin server to Archaius configuration using the prefix
    // 'karyon.rxnetty.admin'. 
    @Provides
    @Singleton
    @AdminServer
    protected HttpServerConfig getAdminServerConfig(AdminServerConfig config) {
        return config; 
    }
    
    @Provides
    @Singleton
    protected AdminServerConfig getAdminServerConfig(ConfigProxyFactory factory) {
        return factory.newProxy(AdminServerConfig.class);
    }
    
    @Override
    public boolean equals(Object obj) {
        return AdminServerModule.class.equals(obj.getClass());
    }

    @Override
    public int hashCode() {
        return AdminServerModule.class.hashCode();
    }
}
