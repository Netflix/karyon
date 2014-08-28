package com.netflix.karyon.archaius;

/**
 * Simple abstraction to allow for property overrides to be loaded.  Overrides are loaded after
 * Archaius properties are loaded but before any other operation is performed in ArchaiusSuite
 * 
 * Note that concrete {@link PropertiesLoader} implementations are loaded within the context
 * of the bootstrap injector so it is possible to inject dependencies such as KaryonBootstrap into 
 * the property loaders.
 * 
 * @author Nitesh Kant
 */
public interface PropertiesLoader {

    void load();
}
