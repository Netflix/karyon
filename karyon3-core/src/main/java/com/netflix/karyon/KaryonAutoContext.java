package com.netflix.karyon;

import java.lang.annotation.Annotation;
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
     * 
     * @param type
     * @param qualifier
     * @return Return true if a binding exists for a type and qualifier
     */
    <T> boolean hasBinding(Class<T> type, Class<? extends Annotation> qualifier);

    /**
     * @param type
     * @return Return true if there exists an injection point for the specified type
     */
    <T> boolean hasInjectionPoint(Class<T> type);

    /**
     * @param type
     * @param qualifier
     * @return Return true if there exists an injection point for the specified type and qualifier
     */
    <T> boolean hasInjectionPoint(Class<T> type, Class<? extends Annotation> qualifier);

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
