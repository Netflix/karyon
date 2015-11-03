package com.netflix.karyon.admin.ui;

import javax.inject.Singleton;

import com.google.inject.Provides;
import com.netflix.archaius.ConfigProxyFactory;
import com.netflix.governator.DefaultModule;
import com.netflix.karyon.admin.AdminUIServer;
import com.netflix.karyon.admin.HttpServerConfig;
import com.netflix.karyon.admin.HttpServerModule;
import com.netflix.karyon.admin.SimpleHttpServer;

public final class AdminUIServerModule extends DefaultModule {
    @Override
    protected void configure() {
        install(new HttpServerModule());
        
        bind(SimpleHttpServer.class).annotatedWith(AdminUIServer.class).to(AdminUIHttpServer.class).asEagerSingleton();
    }
    
    // This binds our admin server to Archaius configuration using the prefix
    // 'karyon.rxnetty.admin'. 
    @Provides
    @Singleton
    @AdminUIServer
    protected HttpServerConfig getAdminUIServerConfig(AdminUIServerConfig config) {
        return config;
    }
    
    @Provides
    @Singleton
    protected AdminUIServerConfig getAdminUIServerConfig(ConfigProxyFactory factory) {
        return factory.newProxy(AdminUIServerConfig.class);
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
