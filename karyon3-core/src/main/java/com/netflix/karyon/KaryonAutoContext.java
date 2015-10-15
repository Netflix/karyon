package com.netflix.karyon;

import java.util.List;
import java.util.Set;

import com.google.inject.spi.Element;

/**
 * Context for the auto module to provide context to any Condition
 */
public interface KaryonAutoContext {  
    /**
     * @param className
     * @return Return true if the module was installed
     */
    boolean hasModule(String className);
    
    /**
     * @param profile
     * @return Return true if profile was set
     */
    boolean hasProfile(String profile);
     
    /**
     * @param type
     * @return Return true if a binding exists for a key
     */
    <T> boolean hasBinding(Class<T> type);

    /**
     * Get all elements that are part of the core modules
     * @return
     */
    List<Element> getElements();

    /**
     * Return a complete list of configured profiles
     * @return
     */
    Set<String> getProfiles();

    /**
     * Return a complete list of modules, including installed modules
     * @return
     */
    Set<String> getModules();
}
