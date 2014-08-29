package com.netflix.karyon.ws.rs;

import javax.inject.Provider;

/**
 * Contract for using a DI framework to construct resources
 * 
 * @author elandau
 *
 */
public interface InjectionSpi {
    /**
     * Construct an instance of the specified type.
     * 
     * @param type
     * @return
     */
    public <T> Provider<T> getProvider(Class<T> type);
    
    /**
     * Construct an instance of type given a InjectionSpi for the request scope
     * @param type
     * @param scoped
     * @return
     */
    public <T> Provider<T> getProvider(Class<T> type, InjectionSpi scoped);
}
