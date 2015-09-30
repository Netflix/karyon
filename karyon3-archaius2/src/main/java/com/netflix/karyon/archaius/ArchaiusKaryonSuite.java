package com.netflix.karyon.archaius;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import com.netflix.archaius.Config;
import com.netflix.archaius.config.CompositeConfig;
import com.netflix.archaius.config.DefaultSettableConfig;
import com.netflix.archaius.config.EnvironmentConfig;
import com.netflix.archaius.config.MapConfig;
import com.netflix.archaius.config.SettableConfig;
import com.netflix.archaius.config.SystemConfig;
import com.netflix.archaius.exceptions.ConfigException;
import com.netflix.archaius.guice.ArchaiusModule;
import com.netflix.archaius.guice.ConfigSeeder;
import com.netflix.archaius.guice.ConfigSeeders;
import com.netflix.archaius.guice.RootLayer;
import com.netflix.archaius.inject.ApplicationLayer;
import com.netflix.archaius.inject.DefaultsLayer;
import com.netflix.archaius.inject.LibrariesLayer;
import com.netflix.archaius.inject.RemoteLayer;
import com.netflix.archaius.inject.RuntimeLayer;
import com.netflix.governator.auto.AbstractPropertySource;
import com.netflix.governator.auto.PropertySource;
import com.netflix.karyon.AbstractKaryonSuite;
import com.netflix.karyon.KaryonSuite;
import com.netflix.karyon.ServerContext;

/**
 * Suite used to configure Archaius2 using a builder and making it available
 * as a Governator PropertySource for conditional module loading as well as
 * enabling it in the application.
 * 
 * @author elandau
 *
 */
public abstract class ArchaiusKaryonSuite extends AbstractKaryonSuite {
    private static final String DEFAULT_CONFIG_NAME     = "application";
    
    private static final String RUNTIME_LAYER_NAME      = "RUNTIME";
    private static final String REMOTE_LAYER_NAME       = "REMOTE";
    private static final String SYSTEM_LAYER_NAME       = "SYSTEM";
    private static final String ENVIRONMENT_LAYER_NAME  = "ENVIRONMENT";
    private static final String APPLICATION_LAYER_NAME  = "APPLICATION";
    private static final String LIBRARIES_LAYER_NAME    = "LIBRARIES";
    private static final String DEFAULTS_LAYER_NAME     = "DEFAULTS";
    
    static {
        System.setProperty("archaius.default.configuration.class",      "com.netflix.archaius.bridge.StaticAbstractConfiguration");
        System.setProperty("archaius.default.deploymentContext.class",  "com.netflix.archaius.bridge.StaticDeploymentContext");
    }

    public static class Builder {
        private String                  configName = DEFAULT_CONFIG_NAME;
        private Config                  applicationOverrides = null;
        private Map<String, Config>     libraryOverrides = new HashMap<>();
        private Set<Config>             runtimeOverrides = new HashSet<>();
        private Set<Config>             defaultSeeders = new HashSet<>();
        private Properties              props = new Properties();
        
        /**
         * Configuration name to use for property loading.  Default configuration
         * name is 'application'.  This value is injectable as
         *  
         * <code>{@literal @}Named("karyon.configName") String configName</code>
         * 
         * @param value
         * @return
         */
        public Builder withConfigName(String value) {
            this.configName = value;
            return this;
        }
        
        public Builder withApplicationName(String value) {
            props.put(ServerContext.APP_ID, value);
            return this;
        }
        
        public Builder withApplicationOverrides(Properties prop) throws ConfigException {
            return withApplicationOverrides(MapConfig.from(prop));
        }
        
        public Builder withApplicationOverrides(Config config) throws ConfigException {
            this.applicationOverrides = config;
            return this;
        }
        
        public Builder withRuntimeOverrides(Properties prop) throws ConfigException {
            return withRuntimeOverrides(MapConfig.from(prop));
        }
        
        public Builder withRuntimeOverrides(Config config) throws ConfigException {
            this.runtimeOverrides.add(config);
            return this;
        }
        
        public Builder withDefaults(Properties prop) throws ConfigException {
            return withDefaults(MapConfig.from(prop));
        }
        
        public Builder withDefaults(Config config) throws ConfigException {
            this.defaultSeeders.add(config);
            return this;
        }
        
        public Builder withLibraryOverrides(String name, Properties prop) throws ConfigException {
            return withLibraryOverrides(name, MapConfig.from(prop));
        }
        
        public Builder withLibraryOverrides(String name, Config config) throws ConfigException {
            this.libraryOverrides.put(name, config);
            return this;
        }
        
        public ArchaiusKaryonSuite build() {
            return new ArchaiusKaryonSuite() {
                @Override
                public void configure() throws Exception {
                    addModule(new ArchaiusModule());
                    
                    if (!props.isEmpty()) {
                        try {
                            withRuntimeOverrides(MapConfig.from(props));
                        } catch (ConfigException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    
                    SettableConfig  runtimeLayer     = new DefaultSettableConfig();
                    SettableConfig  defaultsLayer    = new DefaultSettableConfig();
                    CompositeConfig overrideLayer    = new CompositeConfig();
                    CompositeConfig applicationLayer = new CompositeConfig();
                    CompositeConfig librariesLayer   = new CompositeConfig();
                    
                    if (applicationOverrides != null) {
                        applicationLayer.addConfig("overrides", applicationOverrides);
                    }
                    
                    final CompositeConfig rootConfig = CompositeConfig.builder()
                            .withConfig(RUNTIME_LAYER_NAME,              runtimeLayer)
                            .withConfig(REMOTE_LAYER_NAME,               overrideLayer)
                            .withConfig(SYSTEM_LAYER_NAME,               SystemConfig.INSTANCE)
                            .withConfig(ENVIRONMENT_LAYER_NAME,          EnvironmentConfig.INSTANCE)
                            .withConfig(APPLICATION_LAYER_NAME,          applicationLayer)
                            .withConfig(LIBRARIES_LAYER_NAME,            librariesLayer)
                            .withConfig(DEFAULTS_LAYER_NAME,             defaultsLayer)
                            .build();
                            ;

                    PropertySource propertySource = new AbstractPropertySource() {
                            @Override
                            public String get(String key) {
                                return rootConfig.getString(key, null);
                            }

                            @Override
                            public String get(String key, String defaultValue) {
                                return rootConfig.getString(key, defaultValue);
                            }
                        };

                    setPropertySource(propertySource);
                    
                    addOverrideModule(new AbstractModule() {
                        @Override
                        protected void configure() {
                            bind(SettableConfig.class).annotatedWith(RuntimeLayer.class).toInstance(runtimeLayer);
                            bind(SettableConfig.class).annotatedWith(DefaultsLayer.class).toInstance(defaultsLayer);
                            bind(CompositeConfig.class).annotatedWith(ApplicationLayer.class).toInstance(applicationLayer);
                            bind(CompositeConfig.class).annotatedWith(RemoteLayer.class).toInstance(overrideLayer);
                            bind(CompositeConfig.class).annotatedWith(LibrariesLayer.class).toInstance(librariesLayer);
                            
                            bind(Config.class).annotatedWith(RootLayer.class).toInstance(rootConfig);
                            
                            bind(String.class).annotatedWith(ApplicationLayer.class).toInstance(configName);
                            bindConstant().annotatedWith(Names.named("karyon.configName")).to(configName);
                            
                            MapBinder<String, Config> libraries = MapBinder.newMapBinder(binder(), String.class, Config.class, LibrariesLayer.class);
                            for (Map.Entry<String, Config> config : libraryOverrides.entrySet()) {
                                libraries.addBinding(config.getKey()).toInstance(config.getValue());
                            }
                            
                            Multibinder<ConfigSeeder> runtime = Multibinder.newSetBinder(binder(), ConfigSeeder.class, RuntimeLayer.class);
                            for (Config config : runtimeOverrides) {
                                runtime.addBinding().toInstance(ConfigSeeders.from(config));
                            }
                            
                            Multibinder<ConfigSeeder> defaults = Multibinder.newSetBinder(binder(), ConfigSeeder.class, DefaultsLayer.class);
                            for (Config config : defaultSeeders) {
                                defaults.addBinding().toInstance(ConfigSeeders.from(config));
                            }
                        }
                        
                        @Override
                        public boolean equals(Object obj) {
                            return getClass().equals(obj.getClass());
                        }

                        @Override
                        public int hashCode() {
                            return getClass().hashCode();
                        }

                        @Override
                        public String toString() {
                            return "ArchaiusKaryonConfigurationModule";
                        }
                    });
                }
            };
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }

    public static KaryonSuite createDefault() {
        return builder().build();
    }
}