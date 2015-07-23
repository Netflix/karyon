package com.netflix.karyon.admin.ui;

import java.io.IOException;
import java.util.Collections;

import javax.inject.Singleton;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.netflix.archaius.ConfigProxyFactory;
import com.netflix.karyon.admin.AdminUIServer;
import com.netflix.karyon.admin.HttpServerConfig;
import com.netflix.karyon.admin.HttpServerModule;
import com.netflix.karyon.admin.SimpleHttpServer;
import com.sun.net.httpserver.HttpHandler;

public final class AdminUIServerModule extends AbstractModule {
    @Override
    protected void configure() {
        install(new HttpServerModule());
    }
    
    // This binds our admin server to Archaius configuration using the prefix
    // 'karyon.rxnetty.admin'. 
    @Provides
    @Singleton
    @AdminUIServer
    protected HttpServerConfig getAdminSUIerverConfig(AdminUIServerConfig config) {
        return config;
    }
    
    @Provides
    @Singleton
    protected AdminUIServerConfig getAdminSUIerverConfig(ConfigProxyFactory factory) {
        return factory.newProxy(AdminUIServerConfig.class);
    }
    
    @Provides
    @Singleton
    @AdminUIServer
    protected SimpleHttpServer getAdminUIServer(@AdminUIServer HttpServerConfig config, AdminUIHttpHandler handler) throws IOException {
        return new SimpleHttpServer(config, Collections.<String, HttpHandler>singletonMap("/", handler));
    }
    
    @Override
    public boolean equals(Object obj) {
        return AdminUIServerModule.class.equals(obj.getClass());
    }

    @Override
    public int hashCode() {
        return AdminUIServerModule.class.hashCode();
    }
}
