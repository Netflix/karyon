package com.netflix.karyon.example.rxnetty;

import io.reactivex.netty.protocol.http.server.HttpServer;

import javax.inject.Singleton;

import com.google.inject.Provides;
import com.netflix.archaius.ConfigProxyFactory;
import com.netflix.governator.DefaultModule;
import com.netflix.karyon.rxnetty.ServerConfig;

public class HelloWorldEndpointModule extends DefaultModule {
    // This is our main RxNetty server
    @Provides
    @Singleton
    HttpServer getServer(ServerConfig config, HelloWorldRequestHandler handler) {
        return HttpServer
            .newServer(config.getServerPort())
            .start(handler);
    }
    
    // This binds our main server to Archaius configuration using the default
    // prefix 'karyon.rxnetty' on ServerConfig.  See helloworld.properties
    @Provides
    @Singleton
    ServerConfig getConfig(ConfigProxyFactory factory) {
        return factory.newProxy(ServerConfig.class);
    }
}
