package com.netflix.karyon;

import java.util.List;
import java.util.Set;

import com.google.inject.Module;
import com.google.inject.Stage;
import com.netflix.karyon.api.KaryonFeature;
import com.netflix.karyon.api.KaryonFeatures;
import com.netflix.karyon.api.PropertySource;

/**
 * Configuration contract needed to bootstrap a Governator based application.
 */
public interface KaryonConfiguration {
    /**
     * Return the list of core application modules to be used
     */
    List<Module> getModules();
    
    /**
     * Return a list of override modules to be used as the final override for any bindings
     * specified in modules returned by getModules().  Override modules are useful when an 
     * application has to resolve a binding conflict or when testing.  This method is 
     * recommended over Guice's Modules.override since the later can result in duplicate 
     * bindings due to the loss of context for Guice's built in module de-duping using
     * equals() and hashCode() on a module class.
     * @return
     */
    List<Module> getOverrideModules();
    
    /**
     * Return a list of ModuleListProviders through which modules may be auto-loaded.
     */
    List<ModuleListProvider> getAutoModuleListProviders();
    
    /**
     * Return a list of active profiles for the injector.  These profiles are used when
     * processing @ConditionalOnProfile annotations
     */
    Set<String> getProfiles();

    /**
     * Return the main property source to be used during the bootstrap phase 
     * @return
     */
    PropertySource getPropertySource();
    
    /**
     * Return the Guice injector stage.  The recommended default is Stage.DEVELOPMENT
     * otherwise all singletons are eager, including lazy injection using Provider
     */
    Stage getStage();
    
    /**
     * Determine if a core karyon feature has been enabled.  See {@link KaryonFeatures}
     * for available features.
     */
    boolean isFeatureEnabled(KaryonFeature feature);

    /**
     * Return list of modules with default implementations.
     */
    List<Module> getDefaultModules();
}
