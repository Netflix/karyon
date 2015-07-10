package com.netflix.karyon.archaius;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import com.netflix.archaius.CascadeStrategy;
import com.netflix.archaius.Config;
import com.netflix.archaius.ConfigListener;
import com.netflix.archaius.ConfigProxyFactory;
import com.netflix.archaius.Decoder;
import com.netflix.archaius.PropertyFactory;
import com.netflix.archaius.config.CompositeConfig;
import com.netflix.archaius.config.SettableConfig;
import com.netflix.archaius.guice.ArchaiusConfiguration;
import com.netflix.archaius.guice.ArchaiusModule;
import com.netflix.archaius.guice.ConfigSeeder;
import com.netflix.archaius.guice.OptionalArchaiusConfiguration;
import com.netflix.archaius.guice.RootLayer;
import com.netflix.archaius.inject.ApplicationLayer;
import com.netflix.archaius.inject.DefaultsLayer;
import com.netflix.archaius.inject.LibrariesLayer;
import com.netflix.archaius.inject.RemoteLayer;
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
            final @DefaultsLayer SettableConfig defaultsLayer,
            final PropertyFactory propertyFactory,
            final ConfigProxyFactory proxyFactory,
            final ArchaiusConfiguration archaiusConfig) {
        return ModuleProviders.from(new AbstractModule() {
            @Override
            protected void configure() {
                bind(Config.class).annotatedWith(RootLayer.class).toInstance(config);
                bind(ArchaiusConfiguration.class).annotatedWith(Names.named("bootstrap")).toInstance(archaiusConfig);
                bind(ArchaiusConfiguration.class).to(DelegatingArchaiusConfig.class);
                bind(CascadeStrategy.class).toInstance(archaiusConfig.getCascadeStrategy());
                bind(Decoder.class).toInstance(archaiusConfig.getDecoder());
                
                bind(SettableConfig.class) .annotatedWith(RuntimeLayer.class).toInstance(runtime);
                bind(SettableConfig.class) .annotatedWith(DefaultsLayer.class).toInstance(defaultsLayer);
                bind(CompositeConfig.class).annotatedWith(LibrariesLayer.class).toInstance(libraries);
            }
            
            @Provides
            @Singleton
            Config getConfig(ArchaiusConfiguration archaiusConfiguration,
                    @RootLayer        Config            config,
                    @ApplicationLayer CompositeConfig   applicationLayer,
                    @RemoteLayer      CompositeConfig   remoteLayer,
                    @RuntimeLayer     SettableConfig    runtimeLayer,
                    @DefaultsLayer    SettableConfig    defaultsLayer,
                    ConfigLifecycleListener listener
                ) throws Exception {
                
                listener.setConfig(config);
                
                for (ConfigSeeder provider : archaiusConfiguration.getDefaultsLayerSeeders()) {
                    defaultsLayer.setProperties(provider.get(config));
                }
         
                for (ConfigSeeder provider : archaiusConfiguration.getRuntimeLayerSeeders()) {
                    runtimeLayer.setProperties(provider.get(config));
                }
         
                for (ConfigSeeder provider : archaiusConfiguration.getRemoteLayerSeeders()) {
                    remoteLayer.addConfig("remote", provider.get(config));
                }
                
                return config;
            }
        });
    }
    
    public static class DelegatingArchaiusConfig extends OptionalArchaiusConfiguration {
        @Inject
        @Named("bootstrap")
        ArchaiusConfiguration bootstrapConfig;
        
        @Override
        public String getConfigName() {
            return bootstrapConfig.getConfigName();
        }

        @Override
        public CascadeStrategy getCascadeStrategy() {
            return bootstrapConfig.getCascadeStrategy();
        }

        @Override
        public Decoder getDecoder() {
            return bootstrapConfig.getDecoder();
        }

        @Override
        public Set<ConfigListener> getConfigListeners() {
            throw new IllegalStateException("ConfigListeners will have been handled in the bootstrap layer");
        }

        @Override
        public Config getApplicationOverride() {
            throw new IllegalStateException("ApplicationOverride will have been handled in the bootstrap layer");
        }
        
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
