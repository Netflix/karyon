package com.netflix.karyon.ws.rs;

import javax.inject.Provider;

/**
 * Contract for using a DI framework to construct resources
 * 
 * @author elandau
 *
 */
public interface IoCProviderFactory {
    /**
     * Construct an instance of the specified type.
     * 
     * @param type
     * @return
     */
    public <T> Provider<T> getProvider(Class<T> type);
}
