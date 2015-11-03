package com.netflix.karyon.admin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;

@Singleton
public class GuiceHttpServerRegistry implements HttpServerRegistry {
    final Map<String, Provider<SimpleHttpServer>> servers = new HashMap<>();
    final Map<String, HttpServerConfig> configs = new HashMap<>();
    
    @Inject
    public GuiceHttpServerRegistry(Injector injector) {
        {
            List<Binding<SimpleHttpServer>> bindings = injector.findBindingsByType(TypeLiteral.get(SimpleHttpServer.class));
            for (Binding<SimpleHttpServer> binding : bindings) {
                servers.put(getAnnotationDescription(binding.getKey()), binding.getProvider());
            }
        }
        
        {
            List<Binding<HttpServerConfig>> bindings = injector.findBindingsByType(TypeLiteral.get(HttpServerConfig.class));
            for (Binding<HttpServerConfig> binding : bindings) {
                configs.put(getAnnotationDescription(binding.getKey()), binding.getProvider().get());
            }
        }
    }
    
    private static String getAnnotationDescription(Key<?> key) {
        if (key.getAnnotation() != null) {
            return key.getAnnotation().toString();
        }
        else if (key.getAnnotationType() != null) {
            return key.getAnnotationType().getSimpleName();
        }
        else {
            return "default";
        }
    }
    @Override
    public Map<String, Provider<SimpleHttpServer>> getServers() {
        return servers;
    }

    @Override
    public Map<String, HttpServerConfig> getServerConfigs() {
        return configs;
    }
}
