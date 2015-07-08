package com.netflix.karyon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.google.inject.Module;
import com.google.inject.Stage;
import com.netflix.governator.ModuleListProvider;
import com.netflix.governator.auto.annotations.ConditionalOnProfile;

public class DefaultKaryonConfiguration implements KaryonConfiguration {
    public static abstract class Builder<T extends Builder<T>> {
        protected Stage                       stage = Stage.DEVELOPMENT;
        protected String                      configName = "application";
        protected List<Module>                bootstrapModules = new ArrayList<>();
        protected List<Module>                modules = new ArrayList<>();
        protected Set<String>                 profiles = new LinkedHashSet<>();
        protected List<ModuleListProvider>    moduleProviders = new ArrayList<>();

        /**
         * Module to add to the final injector
         * @param module
         * @return
         */
        public T addModule(Module module) {
            this.modules.add(module);
            return This();
        }
        
        /**
         * Modules to add to the final injector
         * @param modules
         * @return
         */
        public T addModules(Module... modules) {
            this.modules.addAll(Arrays.asList(modules));
            return This();
        }

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
        
        /**
         * Add a module finder such as a ServiceLoaderModuleFinder or ClassPathScannerModuleFinder
         * @param finder
         * @return
         */
        public T addModuleListProvider(ModuleListProvider finder) {
            this.moduleProviders.add(finder);
            return This();
        }
        
        /**
         * Bootstrap overrides for the bootstrap injector used to load and inject into 
         * the conditions.  Bootstrap does not restrict the bindings to allow any type
         * to be externally provided and injected into conditions.  Several simple
         * bindings are provided by default and may be overridden,
         * 1.  Config
         * 2.  Profiles
         * 3.  BoundKeys (TODO)
         * 
         * @param bootstrapModule
         */
        public T addBootstrapModule(Module bootstrapModule) {
            this.bootstrapModules.add(bootstrapModule);
            return This();
        }
        
        public T addBootstrapModules(Module ... bootstrapModule) {
            this.bootstrapModules.addAll(Arrays.asList(bootstrapModule));
            return This();
        }

        public T addBootstrapModules(List<Module> bootstrapModule) {
            this.bootstrapModules.addAll(bootstrapModule);
            return This();
        }

        /**
         * Add a runtime profile.  @see {@link ConditionalOnProfile}
         * 
         * @param profile
         */
        public T addProfile(String profile) {
            this.profiles.add(profile);
            return This();
        }

        /**
         * Add a runtime profiles.  @see {@link ConditionalOnProfile}
         * 
         * @param profile
         */
        public T addProfiles(String... profiles) {
            this.profiles.addAll(Arrays.asList(profiles));
            return This();
        }
        
        /**
         * Add a runtime profiles.  @see {@link ConditionalOnProfile}
         * 
         * @param profile
         */
        public T addProfiles(Collection<String> profiles) {
            this.profiles.addAll(profiles);
            return This();
        }
        
        protected T This() {
            return (T) this;
        }
        
        public KaryonConfiguration build() {
            return new DefaultKaryonConfiguration(this);
        }
    }
    
    private final Stage                       stage;
    private final List<Module>                bootstrapModules;
    private final List<Module>                modules;
    private final Set<String>                 profiles;
    private final List<ModuleListProvider>    moduleProviders;

    public DefaultKaryonConfiguration() {
        this.stage = Stage.DEVELOPMENT;
        this.bootstrapModules = new ArrayList<>();
        this.modules = new ArrayList<>();
        this.profiles = new HashSet<>();
        this.moduleProviders = new ArrayList<>();
    }
    
    protected DefaultKaryonConfiguration(Builder<?> builder) {
        this.stage = builder.stage;
        this.bootstrapModules = new ArrayList<>(builder.bootstrapModules);
        this.modules = new ArrayList<>(builder.modules);
        this.profiles = new LinkedHashSet<>(builder.profiles);
        this.moduleProviders = new ArrayList<>(builder.moduleProviders);
    }
    
    @Override
    public List<Module> getBootstrapModules() {
        return bootstrapModules;
    }

    @Override
    public List<Module> getModules() {
        return modules;
    }

    @Override
    public List<ModuleListProvider> getModuleListProviders() {
        return moduleProviders;
    }

    @Override
    public Set<String> getProfiles() {
        return profiles;
    }

    @Override
    public Stage getStage() {
        return stage;
    }
}
