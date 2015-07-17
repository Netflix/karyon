package com.netflix.karyon.rxnetty;

import io.reactivex.netty.protocol.http.server.HttpServer;

import javax.inject.Singleton;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.netflix.archaius.ConfigProxyFactory;
import com.netflix.karyon.admin.AdminModule;
import com.netflix.karyon.admin.AdminServer;

public class RxNettyAdminServerModule extends AbstractModule {
    @Override
    protected void configure() {
        install(new AdminModule());
        install(new RxNettyModule());
        
        bind(AdminView.class).annotatedWith(AdminServer.class).to(HtmlAdminView.class);
    }
    
    @Provides
    @Singleton
    @AdminServer
    protected HttpServer getAdminServer(@AdminServer ServerConfig config, AdminServerHandler handler) {
        return HttpServer
            .newServer(config.getServerPort())
            .start(handler);
    }
    
    // This binds our admin server to Archaius configuration using the prefix
    // 'karyon.rxnetty.admin'. 
    @Provides
    @Singleton
    @AdminServer
    protected ServerConfig getAdminServerConfig(ConfigProxyFactory factory) {
        return factory.newProxy(AdminServerConfig.class);
    }
    
    @Provides
    @Singleton
    @AdminServer
    protected ObjectMapper getMapper() {
        return new ObjectMapper();
    }

    @Override
    public boolean equals(Object obj) {
        return RxNettyAdminServerModule.class.equals(obj.getClass());
    }

    @Override
    public int hashCode() {
        return RxNettyAdminServerModule.class.hashCode();
    }

}
