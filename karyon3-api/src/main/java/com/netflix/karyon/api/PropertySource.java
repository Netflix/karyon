package com.netflix.karyon.api;


/**
 * Very simple config interface to be used by Conditions to gain access
 * to any type of configuration.  The internal default in AutoModuleBuilder 
 * is to delegate to System properties.
 * 
 * @author elandau
 * 
 */
public interface PropertySource {
    /**
     * Get the value of a property or null if not found
     * 
     * @param key
     * @return
     */
    String get(String key);
    
    /**
     * Get the value of a property or default if not found
     * 
     * @param key
     * @param defaultValue
     * @return
     */
    String get(String key, String defaultValue);
    
    /**
     * Get a property value of a specific type
     * 
     * @param key
     * @param type
     * @return
     */
    <T> T get(String key, Class<T> type);

    /**
     * Get a property value of a specific type while returning a 
     * default value if the property is not set.
     * 
     * @param key
     * @param type
     * @param defaultValue
     * @return
     */
    <T> T get(String key, Class<T> type, T defaultValue);
}
