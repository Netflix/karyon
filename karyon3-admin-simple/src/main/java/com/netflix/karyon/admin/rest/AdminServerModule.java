package com.netflix.karyon.admin.rest;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Singleton;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.multibindings.MapBinder;
import com.netflix.archaius.ConfigProxyFactory;
import com.netflix.karyon.admin.AdminModule;
import com.netflix.karyon.admin.AdminServer;
import com.netflix.karyon.admin.HttpServerConfig;
import com.netflix.karyon.admin.HttpServerModule;
import com.netflix.karyon.admin.SimpleHttpServer;
import com.sun.net.httpserver.HttpHandler;

public final class AdminServerModule extends AbstractModule {
    @Override
    protected void configure() {
        install(new AdminModule());
        install(new HttpServerModule());
        
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
    
    @Provides
    @Singleton
    @AdminServer
    protected SimpleHttpServer getAdminServer(@AdminServer HttpServerConfig config, @AdminServer Map<String, HttpHandler> otherHandlers, AdminHttpHandler adminHandler) throws IOException {
        LinkedHashMap<String, HttpHandler> handlers = new LinkedHashMap<>();
        handlers.putAll(otherHandlers);
        handlers.put("/", adminHandler);
        return new SimpleHttpServer(config, handlers);
    }
    
    @Provides
    @Singleton
    @AdminServer
    protected ObjectMapper getMapper() {
        return new ObjectMapper();
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
