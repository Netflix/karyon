package com.netflix.karyon.admin.internal;

import javax.inject.Singleton;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.netflix.archaius.ConfigProxyFactory;
import com.netflix.karyon.admin.AdminModule;
import com.sun.net.httpserver.HttpHandler;

public class SimpleAdminServerModule extends AbstractModule {
    @Override
    protected void configure() {
        install(new AdminModule());
        
        bind(HttpHandler.class).annotatedWith(SimpleAdmin.class).to(AdminHttpHandler.class);
        bind(AdminHttpServer.class).asEagerSingleton();
    }
    
    // This binds our admin server to Archaius configuration using the prefix
    // 'karyon.rxnetty.admin'. 
    @Provides
    @Singleton
    @SimpleAdmin
    protected AdminServerConfig getAdminServerConfig(ConfigProxyFactory factory) {
        return factory.newProxy(AdminServerConfig.class);
    }
    
    @Provides
    @Singleton
    @SimpleAdmin
    protected ObjectMapper getMapper() {
        return new ObjectMapper();
    }

    @Override
    public boolean equals(Object obj) {
        return SimpleAdminServerModule.class.equals(obj.getClass());
    }

    @Override
    public int hashCode() {
        return SimpleAdminServerModule.class.hashCode();
    }
}
