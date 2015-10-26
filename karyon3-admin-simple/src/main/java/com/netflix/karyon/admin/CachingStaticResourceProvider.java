package com.netflix.karyon.admin;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Decorator for a StaticResourceProvider that caches the results
 */
public class CachingStaticResourceProvider implements StaticResourceProvider {

    private final StaticResourceProvider delegate;
    private final ConcurrentMap<String, Optional<StaticResource>> templates = new ConcurrentHashMap<>();

    public CachingStaticResourceProvider(StaticResourceProvider delegate) {
        this.delegate = delegate;
    }
    
    @Override
    public CompletableFuture<Optional<StaticResource>> getResource(String name) {
        Optional<StaticResource> template = templates.get(name);
        if (template != null) {
            return CompletableFuture.completedFuture(template);
        }
        
        return delegate
                .getResource(name)
                .exceptionally((th)   -> { templates.put(name, Optional.empty()); return Optional.empty(); } )
                .thenApply(    (file) -> { templates.put(name, file); return file; });
    }

}
