package com.netflix.karyon.eureka;

import com.google.inject.AbstractModule;
import com.netflix.appinfo.HealthCheckHandler;

/**
 * Add this module to your project to enable Eureka client and registration
 * 
 * @author elandau
 *
 */
public final class EurekaHealthCheckModule extends AbstractModule {
    @Override
    protected void configure() {
        // Connect Eureka's HealtCheckHandler to injector lifecycle + HealthCheck
        bind(HealthCheckHandler.class).to(KaryonHealthCheckHandler.class);
    }
    
    @Override
    public boolean equals(Object obj) {
        return EurekaHealthCheckModule.class.equals(obj.getClass());
    }

    @Override
    public int hashCode() {
        return EurekaHealthCheckModule.class.hashCode();
    }
}
