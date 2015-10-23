package com.netflix.karyon.admin;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Decorator for a StaticResourceProvider that caches the results
 * 
 * @author elandau
 *
 */
public class CachingStaticResourceProvider implements StaticResourceProvider {

    private final StaticResourceProvider delegate;
    private final ConcurrentMap<String, StaticResource> templates = new ConcurrentHashMap<>();

    public CachingStaticResourceProvider(StaticResourceProvider delegate) {
        this.delegate = delegate;
    }
    
    @Override
    public CompletableFuture<StaticResource> getResource(String name) {
        StaticResource template = templates.get(name);
        if (template != null) {
            return CompletableFuture.completedFuture(template);
        }
        
        return delegate
                .getResource(name)
                .exceptionally((th)   -> { templates.putIfAbsent(name, null); return null; } )
                .thenApply(    (file) -> { templates.putIfAbsent(name, file); return file; });
    }

}
