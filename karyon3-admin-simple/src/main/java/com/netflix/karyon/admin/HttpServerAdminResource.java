package com.netflix.karyon.admin;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import com.google.common.base.Function;
import com.google.common.collect.Maps;

@Singleton
public class HttpServerAdminResource {
    private final HttpServerRegistry registry;
    
    public static interface ServerInfo {
        int getPort();

        String getServerAddress();
    }
    
    @Inject
    public HttpServerAdminResource(HttpServerRegistry registry) {
        this.registry = registry;
    }
    
    public Map<String, ServerInfo> get() {
        return Maps.transformValues(registry.getServers(), new Function<Provider<SimpleHttpServer>, ServerInfo>() {
            @Override
            public ServerInfo apply(final Provider<SimpleHttpServer> provider) {
                return toServerInfo(provider.get());
            }
        });
    }
    
    public ServerInfo get(String name) {
        Provider<SimpleHttpServer> provider = registry.getServers().get(name);
        if (provider == null) {
            return null;
        }
        
        return toServerInfo(provider.get());
    }
    
    private ServerInfo toServerInfo(final SimpleHttpServer server) {
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
