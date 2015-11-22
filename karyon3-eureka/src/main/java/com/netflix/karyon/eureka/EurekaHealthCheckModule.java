package com.netflix.karyon.eureka;

import javax.inject.Singleton;

import com.google.inject.Provides;
import com.netflix.appinfo.HealthCheckHandler;
import com.netflix.archaius.ConfigProxyFactory;
import com.netflix.governator.DefaultModule;
import com.netflix.karyon.eureka.admin.EurekaAdminModule;

/**
 * Add this module to your project to enable Eureka client and registration
 * 
 * @author elandau
 *
 */
public final class EurekaHealthCheckModule extends DefaultModule {
    @Override
    protected void configure() {
        install(new EurekaAdminModule());
        
        // Connect Eureka's HealthCheckHandler to injector lifecycle + HealthCheck
        bind(HealthCheckHandler.class).to(KaryonHealthCheckHandler.class);
    }
    
    @Singleton
    @Provides
    public HealthCheckConfiguration getConfiguration(ConfigProxyFactory factory) {
        return factory.newProxy(HealthCheckConfiguration.class);
    }

    @Override
    public boolean equals(Object obj) {
        return getClass().equals(obj.getClass());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
