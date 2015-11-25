package com.netflix.karyon;

import java.util.Arrays;
import java.util.List;

import com.google.inject.Module;
import com.netflix.karyon.spi.ModuleListProvider;

/**
 * Utility class with convenience methods for creating various standard
 * {@link ModuleListProvider} implementations.
 */
public class ModuleListProviders {
    /**
     * Provider for a fixed pre-defined list of modules
     * @param modules
     * @return
     */
    public static ModuleListProvider forModules(final Module... modules) {
        return forModules(Arrays.asList(modules));
    }
    
    /**
     * Provider for a fixed pre-defined list of modules
     * @param modules
     * @return
     */
    public static ModuleListProvider forModules(final List<Module> modules) {
        return new ModuleListProvider() {
            @Override
            public List<Module> get() {
                return modules;
            }
        };
    }
    
    /**
     * Provider that will use Guava's ClassPath scanner to scan the provided 
     * packages.
     * 
     * @param packages
     * @return
     */
    public static ModuleListProvider forPackages(final String... packages) {
        return new ClassPathModuleListProvider(packages);
    }
    
    /**
     * Provider that will use Guava's ClassPath scanner to scan the provided 
     * packages.
     * 
     * @param packages
     * @return
     */
    public static ModuleListProvider forPackages(final List<String> packages) {
        return new ClassPathModuleListProvider(packages);
    }
    
    /**
     * Provider using the ServiceLoader for class Module
     * 
     * @return
     */
    public static ModuleListProvider forServiceLoader() {
        return new ServiceLoaderModuleListProvider();
    }
    
    /**
     * Provider using the ServiceLoader for class Module
     * 
     * @param type
     * @return
     */
    public static ModuleListProvider forServiceLoader(Class<? extends Module> type) {
        return new ServiceLoaderModuleListProvider(type);
    }
}
