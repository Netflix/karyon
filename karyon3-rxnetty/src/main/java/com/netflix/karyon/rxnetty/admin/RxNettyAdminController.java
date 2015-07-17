package com.netflix.karyon.rxnetty.admin;

import io.reactivex.netty.protocol.http.server.HttpServer;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.netflix.karyon.admin.rest.Controller;
import com.netflix.karyon.rxnetty.RxNettyServerRegistry;

@Singleton
public class RxNettyAdminController implements Controller {
    private final RxNettyServerRegistry registry;
    
    public static interface ServerInfo {
        int getPort();

        String getServerAddress();
    }
    
    @Inject
    public RxNettyAdminController(RxNettyServerRegistry registry) {
        this.registry = registry;
    }
    
    @SuppressWarnings("rawtypes")
    public Map<String, ServerInfo> list() {
        return Maps.transformValues(registry.getServers(), new Function<Provider<HttpServer>, ServerInfo>() {
            @Override
            public ServerInfo apply(final Provider<HttpServer> provider) {
                final HttpServer server = provider.get();
                return new ServerInfo() {
                    @Override
                    public int getPort() {
                        return server.getServerPort();
                    }
                    
                    @Override
                    public String getServerAddress() {
                        return server.getServerAddress().toString();
                    }
                };
            }
        });
    }
    
    public ServerInfo find(String name) {
        Provider<HttpServer> provider = registry.getServers().get(name);
        if (provider == null) {
            return null;
        }
        
        final HttpServer server = provider.get();
        return new ServerInfo() {
            @Override
            public int getPort() {
                return server.getServerPort();
            }
            
            @Override
            public String getServerAddress() {
                return server.getServerAddress().toString();
            }
        };
    }
}
