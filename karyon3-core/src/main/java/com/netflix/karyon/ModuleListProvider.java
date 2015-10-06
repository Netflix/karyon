package com.netflix.karyon;

import java.util.List;

import com.google.inject.Module;

/**
 * Plugin interface for a module provider, such as a ClassPathScannerModuleProvider or 
 * a ServiceLoaderModuleProvider.
 */
public interface ModuleListProvider {
    /**
     * 
     * @return
     */
    List<Module> get();
}
