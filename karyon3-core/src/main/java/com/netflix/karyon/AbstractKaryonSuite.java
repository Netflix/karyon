package com.netflix.karyon;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.google.inject.Module;
import com.google.inject.Stage;
import com.netflix.governator.GovernatorFeature;
import com.netflix.governator.auto.ModuleListProvider;
import com.netflix.governator.auto.PropertySource;

public abstract class AbstractKaryonSuite implements KaryonSuite {
    private KaryonBuilder builder;
    
    @Override
    final public void configure(KaryonBuilder builder) throws Exception {
        this.builder = builder;
        configure();
        this.builder = null;
    }
    
    protected abstract void configure() throws Exception;
    
    protected void addModuleListProvider(ModuleListProvider finder) {
        builder.addModuleListProvider(finder);
    }
    
    protected void addModule(Module module) {
        builder.addModule(module);
    }
    
    protected void addModules(Module ... modules) {
        builder.addModules(Arrays.asList(modules));
    }
    
    protected void addModules(List<Module> modules) {
        builder.addModules(modules);
    }
    
    protected void addOverrideModule(Module module) {
        builder.addOverrideModule(module);
    }
    
    protected void addOverrideModules(Module ... modules) {
        builder.addOverrideModules(Arrays.asList(modules));
    }
    
    protected void addOverrideModules(List<Module> modules) {
        builder.addOverrideModules(modules);
    }
    
    protected void addProfile(String profile) {
        builder.addProfile(profile);
    }

    protected void addProfiles(String... profiles) {
        if (profiles != null) {
            builder.addProfiles(Arrays.asList(profiles));
        }
    }
    
    protected void addProfiles(Collection<String> profiles) {
        builder.addProfiles(profiles);
    }
    
    protected void inStage(Stage stage) {
        builder.inStage(stage);
    }
    
    protected void enable(GovernatorFeature feature) {
        builder.enable(feature);
    }

    protected void disable(GovernatorFeature feature) {
        builder.disable(feature);
    }
    
    protected void setPropertySource(PropertySource propertySource) {
        builder.withPropertySource(propertySource);
    }

    protected PropertySource getPropertySource() {
        return builder.getPropertySource();
    }

}
