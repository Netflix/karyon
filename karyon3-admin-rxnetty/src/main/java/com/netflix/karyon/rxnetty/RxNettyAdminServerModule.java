package com.netflix.karyon.rxnetty;

import io.reactivex.netty.protocol.http.server.HttpServer;

import javax.inject.Singleton;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.netflix.archaius.ConfigProxyFactory;
import com.netflix.karyon.admin.AdminModule;

public class RxNettyAdminServerModule extends AbstractModule {
    @Override
    protected void configure() {
        install(new AdminModule());
        install(new RxNettyModule());
        
        bind(AdminView.class).annotatedWith(RxNettyAdmin.class).to(HtmlAdminView.class);
    }
    
    @Provides
    @Singleton
    @RxNettyAdmin
    protected HttpServer getAdminServer(@RxNettyAdmin ServerConfig config, AdminEndpointHandler handler) {
        return HttpServer
            .newServer(config.getServerPort())
            .start(handler);
    }
    
    // This binds our main server to Archaius configuration using the prefix
    // 'karyon.rxnetty.shutdown'. See helloworld.properties
    @Provides
    @Singleton
    @RxNettyAdmin
    protected ServerConfig getShutdownConfig(ConfigProxyFactory factory) {
        return factory.newProxy(AdminServerConfig.class);
    }
    
    @Provides
    @Singleton
    @RxNettyAdmin
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
