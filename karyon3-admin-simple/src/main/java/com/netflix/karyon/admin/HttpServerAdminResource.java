package com.netflix.karyon.admin;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import com.google.common.base.Function;
import com.google.common.collect.Maps;

@Singleton
@AdminService(name="http", index="list")
public class HttpServerAdminResource {
    private final HttpServerRegistry registry;
    
    @Inject
    public HttpServerAdminResource(HttpServerRegistry registry) {
        this.registry = registry;
    }
    
    public Map<String, SimpleHttpServer> list() {
        return Maps.transformValues(registry.getServers(), new Function<Provider<SimpleHttpServer>, SimpleHttpServer>() {
            @Override
            public SimpleHttpServer apply(final Provider<SimpleHttpServer> provider) {
                return provider.get();
            }
        });
    }
}
