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
import com.netflix.karyon.DefaultKaryonConfiguration;
import com.netflix.karyon.ServerContext;

/**
 * Extension to the archaius configuration which includes bootstrapping of archaius2.
 * 
 * @author elandau
 *
 */
public class ArchaiusKaryonConfiguration extends DefaultKaryonConfiguration {
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

    public static abstract class Builder<T extends Builder<T>> extends DefaultKaryonConfiguration.Builder<T> {
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
         *      @Named("karyon.configName") String configName
         * 
         * @param value
         * @return
         */
        public T withConfigName(String value) {
            this.configName = value;
            return This();
        }
        
        public T withApplicationName(String value) {
            props.put(ServerContext.APP_ID, value);
            return This();
        }
        
        public T withApplicationOverrides(Properties prop) throws ConfigException {
            return withApplicationOverrides(MapConfig.from(prop));
        }
        
        public T withApplicationOverrides(Config config) throws ConfigException {
            this.applicationOverrides = config;
            return This();
        }
        
        public T withRuntimeOverrides(Properties prop) throws ConfigException {
            return withRuntimeOverrides(MapConfig.from(prop));
        }
        
        public T withRuntimeOverrides(Config config) throws ConfigException {
            this.runtimeOverrides.add(config);
            return This();
        }
        
        public T withDefaults(Properties prop) throws ConfigException {
            return withDefaults(MapConfig.from(prop));
        }
        
        public T withDefaults(Config config) throws ConfigException {
            this.defaultSeeders.add(config);
            return This();
        }
        
        public T withLibraryOverrides(String name, Properties prop) throws ConfigException {
            return withLibraryOverrides(name, MapConfig.from(prop));
        }
        
        public T withLibraryOverrides(String name, Config config) throws ConfigException {
            this.libraryOverrides.put(name, config);
            return This();
        }
        
        
        public ArchaiusKaryonConfiguration build() throws Exception {
            initialize(); 
            return new ArchaiusKaryonConfiguration(this);
        }
        
        protected void initialize() throws Exception {
            super.initialize();
            
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

            this.withPropertySource(propertySource);
            
            this.addOverrideModule(new AbstractModule() {
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
                public String toString() {
                    return "ArchaiusKaryonConfigurationModule";
                }
            });
        };
    }
    
    private static class BuilderWrapper extends Builder<BuilderWrapper> {
        @Override
        protected BuilderWrapper This() {
            return this;
        }
    }
    
    public static Builder<?> builder() {
        return new BuilderWrapper();
    }
    
    public ArchaiusKaryonConfiguration() {
        this(builder());
    }
    
    private ArchaiusKaryonConfiguration(Builder<?> builder) {
        super(builder);
    }
}