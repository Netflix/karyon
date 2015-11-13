package com.netflix.karyon.rxnetty.admin;

import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.http.server.HttpServer;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.netflix.karyon.admin.AdminService;
import com.netflix.karyon.rxnetty.server.RxNettyHttpServerRegistry;

@Singleton
@AdminService(name="netty", index="current")
public class RxNettyAdminResource {
    private final RxNettyHttpServerRegistry registry;
    
    public static interface ServerInfo {
        int getPort();
    }
    
    @Inject
    public RxNettyAdminResource(RxNettyHttpServerRegistry registry) {
        this.registry = registry;
    }
    
    public Map<String, ServerInfo> list() {
        return Maps.transformValues(registry.getServers(), new Function<Provider<HttpServer<ByteBuf, ByteBuf>>, ServerInfo>() {
            @Override
            public ServerInfo apply(final Provider<HttpServer<ByteBuf, ByteBuf>> provider) {
                final HttpServer<ByteBuf, ByteBuf> server = provider.get();
                return new ServerInfo() {
                    @Override
                    public int getPort() {
                        return server.getServerPort();
                    }
                };
            }
        });
    }
    
    public static class GetRequest {
        String name;
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getName() {
            return name;
        }
    }
    
    // rxnetty/:name
    public ServerInfo get(GetRequest request) {
        Provider<HttpServer<ByteBuf, ByteBuf>> provider = registry.getServers().get(request.getName());
        if (provider == null) {
            return null;
        }
        
        final HttpServer<ByteBuf, ByteBuf> server = provider.get();
        return new ServerInfo() {
            @Override
            public int getPort() {
                return server.getServerPort();
            }
        };
    }
}
