package com.netflix.karyon.rxnetty;

import io.reactivex.netty.protocol.http.server.HttpServer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;

@Singleton
public class GuiceRxNettyServerRegistry implements RxNettyServerRegistry {
    final Map<String, Provider<HttpServer>> servers = new HashMap<>();
    final Map<String, ServerConfig> configs = new HashMap<>();
    
    @SuppressWarnings("rawtypes")
    @Inject
    public GuiceRxNettyServerRegistry(Injector injector) {
        {
            List<Binding<HttpServer>> bindings = injector.findBindingsByType(TypeLiteral.get(HttpServer.class));
            for (Binding<HttpServer> binding : bindings) {
                servers.put(
                        binding.getKey().getAnnotation() != null ? binding.getKey().getAnnotation().toString() : "default", 
                        binding.getProvider());
            }
        }
        
        {
            List<Binding<ServerConfig>> bindings = injector.findBindingsByType(TypeLiteral.get(ServerConfig.class));
            for (Binding<ServerConfig> binding : bindings) {
                configs.put(
                        binding.getKey().getAnnotation() != null ? binding.getKey().getAnnotation().toString() : "default", 
                        binding.getProvider().get());
            }
        }
    }
    
    @Override
    public Map<String, Provider<HttpServer>> getServers() {
        return servers;
    }

    @Override
    public Map<String, ServerConfig> getServerConfigs() {
        return configs;
    }

}
