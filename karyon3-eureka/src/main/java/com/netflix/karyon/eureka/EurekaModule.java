package com.netflix.karyon.eureka;

import com.google.inject.AbstractModule;
import com.netflix.appinfo.EurekaInstanceConfig;
import com.netflix.appinfo.HealthCheckHandler;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.appinfo.providers.EurekaConfigBasedInstanceInfoProvider;
import com.netflix.archaius.guice.ArchaiusModule;
import com.netflix.discovery.DiscoveryClient;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.EurekaClientConfig;

/**
 * Add this module to your project to enable Eureka client and registration
 * 
 * @author elandau
 *
 */
public final class EurekaModule extends AbstractModule {
    @Override
    protected void configure() {
    	install(new ArchaiusModule());
    	
    	// Bindings for eureka
        bind(EurekaInstanceConfig.class).to(KaryonEurekaInstanceConfig.class);
        bind(EurekaClientConfig.class).to(KaryonEurekaClientConfig.class);
        bind(InstanceInfo.class).toProvider(EurekaConfigBasedInstanceInfoProvider.class);
        bind(EurekaClient.class).to(DiscoveryClient.class);

        // Connect Eureka's HealtCheckHandler to injector lifecycle + HealthCheck
        bind(HealthCheckHandler.class).to(KaryonHealthCheckHandler.class);
    }
    
    @Override
    public boolean equals(Object obj) {
        return EurekaModule.class.equals(obj.getClass());
    }

    @Override
    public int hashCode() {
        return EurekaModule.class.hashCode();
    }
}
