package com.netflix.karyon.spi;

import com.netflix.karyon.Karyon;

/**
 * Contract for adding functionality to Karyon as part of adding a Guice module.  This interface
 * must be implemented by a module added to a call to {@link Karyon#addModules(java.util.List)}
 * to avoid having to make multiple calls to Karyon when adding functionality. 
 * 
 * <code>
 * public class FooModule extends AbstractModule implements KaryonModule {
 *    {@literal @}Override
 *    protected void configure() {
 *        // ... Bindings for Guice
 *    }
 *    
 *    {@literal @}Override
 *    public void configure(KaryonBinder binder) {
 *        // ... calls to extend/configure Karyon
 *    }
 * }
 * </code>
 * 
 * @author elandau
 *
 */
public interface KaryonModule {
    void configure(KaryonBinder binder);
}
