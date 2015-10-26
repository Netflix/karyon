package com.netflix.karyon.admin;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Very basic contract for loading a resources.
 * 
 * @author elandau
 */
public interface StaticResourceProvider {
    CompletableFuture<Optional<StaticResource>> getResource(String name);
}
