package com.netflix.karyon.eureka;

import com.google.inject.AbstractModule;
import com.google.inject.binder.LinkedBindingBuilder;
import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.appinfo.EurekaInstanceConfig;
import com.netflix.appinfo.providers.CloudInstanceConfigProvider;
import com.netflix.discovery.DiscoveryClient;
import com.netflix.discovery.EurekaClientConfig;
import com.netflix.discovery.EurekaNamespace;
import com.netflix.discovery.providers.DefaultEurekaClientConfigProvider;
import com.netflix.governator.guice.LifecycleInjectorBuilder;
import com.netflix.governator.guice.LifecycleInjectorBuilderSuite;
import com.netflix.karyon.health.HealthCheckRegistry;

/**
 * @author Nitesh Kant
 */
public class KaryonEurekaModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(com.netflix.appinfo.HealthCheckHandler.class).to(EurekaHealthCheckHandler.class);
        bind(ApplicationInfoManager.class).asEagerSingleton();
        bind(DiscoveryClient.class).asEagerSingleton();
        bind(HealthCheckRegistry.class).to(DefaultHealthCheckRegistry.class);

        configureEureka();
    }

    protected void configureEureka() {
        bindEurekaNamespace().toInstance("eureka.");
        bindEurekaInstanceConfig().toProvider(CloudInstanceConfigProvider.class);
        bindEurekaClientConfig().toProvider(DefaultEurekaClientConfigProvider.class);
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

    public static LifecycleInjectorBuilderSuite asSuite() {
        return new LifecycleInjectorBuilderSuite() {
            @Override
            public void configure(LifecycleInjectorBuilder builder) {
                builder.withAdditionalModules(new KaryonEurekaModule());
            }
        };
    }
}
