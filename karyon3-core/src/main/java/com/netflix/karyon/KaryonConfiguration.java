package com.netflix.karyon;

import java.util.List;
import java.util.Set;

import com.google.inject.Module;
import com.google.inject.Stage;
import com.netflix.governator.ModuleListProvider;

public interface KaryonConfiguration {
    List<Module> getBootstrapModules();
    
    List<Module> getModules();
    
    List<ModuleListProvider> getModuleListProviders();
    
    Set<String> getProfiles();
    
    Stage getStage();
}
