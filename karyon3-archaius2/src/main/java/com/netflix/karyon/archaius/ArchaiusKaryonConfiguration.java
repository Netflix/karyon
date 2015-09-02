package com.netflix.karyon.archaius;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import com.netflix.archaius.Config;
import com.netflix.archaius.config.MapConfig;
import com.netflix.archaius.exceptions.ConfigException;
import com.netflix.archaius.guice.ConfigSeeder;
import com.netflix.archaius.guice.ConfigSeeders;
import com.netflix.archaius.inject.ApplicationOverrideLayer;
import com.netflix.archaius.inject.DefaultsLayer;
import com.netflix.archaius.inject.LibrariesLayer;
import com.netflix.archaius.inject.RuntimeLayer;
import com.netflix.karyon.DefaultKaryonConfiguration;
import com.netflix.karyon.ServerContext;

public class ArchaiusKaryonConfiguration extends DefaultKaryonConfiguration {
    private static final String DEFAULT_CONFIG_NAME = "application";
    
    public static abstract class Builder<T extends Builder<T>> extends DefaultKaryonConfiguration.Builder<T> {
        private String                  configName = DEFAULT_CONFIG_NAME;
        private Config                  applicationOverrides = null;
        private Map<String, Config>     libraryOverrides = new HashMap<>();
        private Set<Config>             runtimeOverrides = new HashSet<>();
        private Set<Config>             defaults = new HashSet<>();
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
            this.defaults.add(config);
            return This();
        }
        
        public T withLibraryOverrides(String name, Properties prop) throws ConfigException {
            return withLibraryOverrides(name, MapConfig.from(prop));
        }
        
        public T withLibraryOverrides(String name, Config config) throws ConfigException {
            this.libraryOverrides.put(name, config);
            return This();
        }
        
        public ArchaiusKaryonConfiguration build() {
            if (!props.isEmpty()) {
                try {
                    withRuntimeOverrides(MapConfig.from(props));
                } catch (ConfigException e) {
                    throw new RuntimeException(e);
                }
            }
            return new ArchaiusKaryonConfiguration(this);
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
    
    private final String configName;
    
    // TODO: Should we make these immutable
    private final Config              applicationOverrides;
    private final Map<String, Config> libraryOverrides;
    private final Set<Config>         runtimeOverrides;
    private final Set<Config>         defaultOverrides;

    
    public ArchaiusKaryonConfiguration() {
        super();
        
        configName = DEFAULT_CONFIG_NAME;
        applicationOverrides = null;
        libraryOverrides = new HashMap<>();
        runtimeOverrides = new HashSet<>();
        defaultOverrides = new HashSet<>();
    }
    
    private ArchaiusKaryonConfiguration(@SuppressWarnings("rawtypes") Builder<?> builder) {
        super(builder);
        this.configName = builder.configName;
        this.applicationOverrides = builder.applicationOverrides;
        this.libraryOverrides = builder.libraryOverrides;
        this.runtimeOverrides = builder.runtimeOverrides;
        this.defaultOverrides = builder.defaults;
    }
    
    public List<Module> getBootstrapModules() {
        List<Module> modules = super.getBootstrapModules();
        modules.add(new ArchaiusBootstrapModule()); 
        modules.add(new AbstractModule() {
            @Override
            protected void configure() {
                this.bindConstant().annotatedWith(Names.named("karyon.configName")).to(getConfigurationName());
                
                Config appOverride = getApplicationOverrides();
                if (appOverride != null) {
                    bind(Key.get(Config.class, ApplicationOverrideLayer.class)).toInstance(appOverride);
                }
                
                MapBinder<String, Config> libraries = MapBinder.newMapBinder(binder(), String.class, Config.class, LibrariesLayer.class);
                for (Map.Entry<String, Config> config : getLibraryOverrides().entrySet()) {
                    libraries.addBinding(config.getKey()).toInstance(config.getValue());
                }
                
                Multibinder<ConfigSeeder> runtime = Multibinder.newSetBinder(binder(), ConfigSeeder.class, RuntimeLayer.class);
                for (Config config : getRuntimeOverrides()) {
                    runtime.addBinding().toInstance(ConfigSeeders.from(config));
                }
                
                Multibinder<ConfigSeeder> defaults = Multibinder.newSetBinder(binder(), ConfigSeeder.class, DefaultsLayer.class);
                for (Config config : getDefaultOverrides()) {
                    defaults.addBinding().toInstance(ConfigSeeders.from(config));
                }
            }
        });
        
        return modules;
    }
    
    public Config getApplicationOverrides() {
        return this.applicationOverrides;
    }
    
    public Set<Config> getRuntimeOverrides() {
        return this.runtimeOverrides;
    }
    
    public Set<Config> getDefaultOverrides() {
        return this.defaultOverrides;
    }
    
    public Map<String, Config> getLibraryOverrides() {
        return this.libraryOverrides;
    }
    
    public String getConfigurationName() {
        return configName;
    }
}
