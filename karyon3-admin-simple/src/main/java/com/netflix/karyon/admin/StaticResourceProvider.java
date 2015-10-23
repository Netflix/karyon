package com.netflix.karyon.admin;

import java.util.concurrent.CompletableFuture;

/**
 * Very basic contract for loading a resources.
 * 
 * @author elandau
 */
public interface StaticResourceProvider {
    CompletableFuture<StaticResource> getResource(String name);
}
