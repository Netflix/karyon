package com.netflix.karyon.eureka;

import javax.inject.Singleton;

import com.google.inject.Provides;
import com.netflix.appinfo.HealthCheckHandler;
import com.netflix.archaius.ConfigProxyFactory;
import com.netflix.governator.DefaultModule;

/**
 * Add this module to your project to enable Eureka client and registration
 * 
 * @author elandau
 *
 */
public final class EurekaHealthCheckModule extends DefaultModule {
    @Override
    protected void configure() {
        // Connect Eureka's HealtCheckHandler to injector lifecycle + HealthCheck
        bind(HealthCheckHandler.class).to(KaryonHealthCheckHandler.class);
    }
    
    @Singleton
    @Provides
    public HealthCheckConfiguration getConfiguration(ConfigProxyFactory factory) {
        return factory.newProxy(HealthCheckConfiguration.class);
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
