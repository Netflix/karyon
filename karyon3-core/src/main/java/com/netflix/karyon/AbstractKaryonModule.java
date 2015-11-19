package com.netflix.karyon;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.google.inject.Module;
import com.google.inject.Stage;
import com.netflix.karyon.api.KaryonFeature;
import com.netflix.karyon.api.PropertySource;

public abstract class AbstractKaryonModule implements KaryonModule {
    private Karyon karyon;
    
    @Override
    final public void configure(Karyon karyon) {
        this.karyon = karyon;
        try {
            configure();
        }
        finally {
            this.karyon = null;
        }
    }
    
    protected abstract void configure();
    
    protected void addAutoModuleListProvider(ModuleListProvider finder) {
        karyon.addAutoModuleListProvider(finder);
    }
    
    protected void addModules(Module ... modules) {
        karyon.addModules(Arrays.asList(modules));
    }
    
    protected void addModules(List<Module> modules) {
        karyon.addModules(modules);
    }
    
    protected void addOverrideModules(Module ... modules) {
        karyon.addOverrideModules(Arrays.asList(modules));
    }
    
    protected void addOverrideModules(List<Module> modules) {
        karyon.addOverrideModules(modules);
    }
    
    protected void addProfile(String profile) {
        karyon.addProfile(profile);
    }

    protected void addProfiles(String... profiles) {
        if (profiles != null) {
            karyon.addProfiles(Arrays.asList(profiles));
        }
    }
    
    protected void addProfiles(Collection<String> profiles) {
        karyon.addProfiles(profiles);
    }
    
    protected void inStage(Stage stage) {
        karyon.inStage(stage);
    }
    
    protected void enableFeature(KaryonFeature feature) {
        karyon.enableFeature(feature);
    }

    protected void disableFeature(KaryonFeature feature) {
        karyon.disableFeature(feature);
    }
    
    protected void setPropertySource(PropertySource propertySource) {
        karyon.setPropertySource(propertySource);
    }

    protected PropertySource getPropertySource() {
        return karyon.getPropertySource();
    }
    
    protected Karyon getKaryon() {
        return karyon;
    }

}
