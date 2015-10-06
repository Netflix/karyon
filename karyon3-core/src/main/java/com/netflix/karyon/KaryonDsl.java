package com.netflix.karyon;

import java.util.Collection;
import java.util.List;

import com.google.inject.Module;
import com.google.inject.Stage;

public interface KaryonDsl<T> {
    T addModules(Module ... modules);
    T addModules(List<Module> modules);
    T addOverrideModules(Module ... modules);
    T addOverrideModules(List<Module> modules);
    T inStage(Stage stage);
    T addAutoModuleListProvider(ModuleListProvider finder);
    T addProfile(String profile);
    T addProfiles(String... profiles);
    T addProfiles(Collection<String> profiles);
    T enableFeature(KaryonFeature feature);
    T disableFeature(KaryonFeature feature);
    T setPropertySource(PropertySource propertySource);
    T using(KaryonModule suite) throws Exception;
    PropertySource getPropertySource();
}
