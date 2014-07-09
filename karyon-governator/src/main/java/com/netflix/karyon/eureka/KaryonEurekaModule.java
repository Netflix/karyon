package com.netflix.karyon.eureka;

import com.google.inject.AbstractModule;
import com.google.inject.binder.LinkedBindingBuilder;
import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.appinfo.EurekaInstanceConfig;
import com.netflix.discovery.DiscoveryClient;
import com.netflix.discovery.EurekaClientConfig;
import com.netflix.discovery.EurekaNamespace;
import com.netflix.discovery.shared.LookupService;

/**
 * @author Nitesh Kant
 */
public class KaryonEurekaModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(com.netflix.appinfo.HealthCheckHandler.class).to(EurekaHealthCheckHandler.class);
        bind(ApplicationInfoManager.class).asEagerSingleton();
        bind(LookupService.class).to(DiscoveryClient.class).asEagerSingleton();

        configureEureka();

        // We should be able to write code that checks whether bindings were supplied and bind default
        // implementations if not
    }

    protected void configureEureka() {
    }

    protected LinkedBindingBuilder<EurekaInstanceConfig> bindEurekaInstanceConfig() {
        return bind(EurekaInstanceConfig.class);
    }

    protected LinkedBindingBuilder<EurekaClientConfig > bindEurekaClientConfig() {
        return bind(EurekaClientConfig.class);
    }

    protected LinkedBindingBuilder<String> bindEurekaNamespace() {
        return bind(String.class).annotatedWith(EurekaNamespace.class);
    }
}
