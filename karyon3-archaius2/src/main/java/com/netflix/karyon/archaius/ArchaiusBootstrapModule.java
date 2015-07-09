package com.netflix.karyon.archaius;

import javax.inject.Named;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.netflix.archaius.CascadeStrategy;
import com.netflix.archaius.Config;
import com.netflix.archaius.ConfigProxyFactory;
import com.netflix.archaius.Decoder;
import com.netflix.archaius.PropertyFactory;
import com.netflix.archaius.config.CompositeConfig;
import com.netflix.archaius.config.SettableConfig;
import com.netflix.archaius.guice.ArchaiusConfiguration;
import com.netflix.archaius.guice.ArchaiusModule;
import com.netflix.archaius.inject.ApplicationLayer;
import com.netflix.archaius.inject.LibrariesLayer;
import com.netflix.archaius.inject.RuntimeLayer;
import com.netflix.governator.auto.AbstractPropertySource;
import com.netflix.governator.auto.ModuleProvider;
import com.netflix.governator.auto.ModuleProviders;
import com.netflix.governator.auto.PropertySource;

public class ArchaiusBootstrapModule extends AbstractModule {
    @Override
    protected void configure() {
        install(new ArchaiusModule());
    }
    
    @Provides
    @Singleton
    @ApplicationLayer
    String getConfigName(@Named("karyon.configName") String configName) {
        return configName;
    }
    
    @Provides
    @Named("archaius")
    @Singleton
    ModuleProvider getModule(
            final Config config, 
            final @RuntimeLayer SettableConfig runtime, 
            final @LibrariesLayer CompositeConfig libraries, 
            final PropertyFactory propertyFactory,
            final ConfigProxyFactory proxyFactory,
            final ArchaiusConfiguration archaiusConfig) {
        return ModuleProviders.from(new AbstractModule() {
            @Override
            protected void configure() {
                bind(Config.class).toInstance(config);
                bind(CascadeStrategy.class).toInstance(archaiusConfig.getCascadeStrategy());
                bind(SettableConfig.class).annotatedWith(RuntimeLayer.class).toInstance(runtime);
                bind(CompositeConfig.class).annotatedWith(LibrariesLayer.class).toInstance(libraries);
                bind(PropertyFactory.class).toInstance(propertyFactory);
                bind(ConfigProxyFactory.class).toInstance(proxyFactory);
                bind(Decoder.class).toInstance(archaiusConfig.getDecoder());
            }
        });
    }
    
    @Provides
    @Singleton
    PropertySource getPropertySource(final Config config) {
        return new AbstractPropertySource() {
            @Override
            public String get(String key) {
                return config.getString(key, null);
            }
        };
    }
    
    @Override 
    public boolean equals(Object obj) {
        return obj != null && getClass().equals(obj.getClass());
    }

    @Override 
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
      return getClass().getName();
    }
}
