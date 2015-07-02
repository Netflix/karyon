package com.netflix.karyon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.Stage;
import com.google.inject.name.Names;
import com.google.inject.util.Modules;
import com.netflix.governator.Governator;
import com.netflix.governator.LifecycleInjector;
import com.netflix.governator.ModuleListProvider;
import com.netflix.governator.auto.AutoModuleBuilder;
import com.netflix.governator.auto.annotations.ConditionalOnProfile;

public class Karyon {
    private final Stage stage = Stage.DEVELOPMENT;
    private final AutoModuleBuilder builder;
    private String configName = "application";
    private List<Module> bootstrapModules = new ArrayList<>();

    private Karyon(Module module) {
        builder = new AutoModuleBuilder(module);
    }
    
    public static Karyon forModule(Module module) {
        return new Karyon(module);
    }
    
    public static Karyon forModules(Module... modules) {
        return new Karyon(Modules.combine(modules));
    }
    
    /**
     * Add a module finder such as a ServiceLoaderModuleFinder or ClassPathScannerModuleFinder
     * @param finder
     * @return
     */
    public Karyon withModuleFinder(ModuleListProvider finder) {
        builder.withModuleFinder(finder);
        return this;
    }
    
    public Karyon withBootstrapModule(Module bootstrapModule) {
        bootstrapModules.add(bootstrapModule);
        return this;
    }
    
    public Karyon withBootstrapModules(Module ... bootstrapModule) {
        bootstrapModules.addAll(bootstrapModules);
        return this;
    }

    /**
     * Add a runtime profile.  @see {@link ConditionalOnProfile}
     * 
     * @param profile
     */
    public Karyon withProfile(String profile) {
        builder.withProfile(profile);
        return this;
    }

    /**
     * Add a runtime profiles.  @see {@link ConditionalOnProfile}
     * 
     * @param profile
     */
    public Karyon withProfiles(String... profiles) {
        builder.withProfiles(profiles);
        return this;
    }

    /**
     * Add a runtime profiles.  @see {@link ConditionalOnProfile}
     * 
     * @param profile
     */
    public Karyon withProfiles(Collection<String> profiles) {
        builder.withProfiles(profiles);
        return this;
    }
    
    public Karyon withConfigName(String value) {
        this.configName = value;
        return this;
    }
    
    public LifecycleInjector create() {
        bootstrapModules.add(new AbstractModule() {
            @Override
            protected void configure() {
                this.bindConstant().annotatedWith(Names.named("configName")).to(configName);
            }
        });
        
        return Governator.createInjector(stage, builder.withBootstrap(Modules.combine(bootstrapModules)).build());
    }
}
