package com.netflix.karyon;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

import com.google.inject.Module;

/**
 * Load Module.class modules from the ServerLoader but filter out any modules
 * that have no profile condition.
 * 
 * @author elandau
 *
 */
public class ServiceLoaderModuleListProvider implements ModuleListProvider {
    
    private final Class<? extends Module> type;
    
    public ServiceLoaderModuleListProvider(Class<? extends Module> type) {
        this.type = type;
    }
    
    public ServiceLoaderModuleListProvider() {
        this(Module.class);
    }

    @Override
    public List<Module> get() {
        List<Module> modules = new ArrayList<>();
        Iterator<? extends Module> iter = ServiceLoader.load(type).iterator();
        while (iter.hasNext()) {
            modules.add(iter.next());
        }
        
        return modules;
    }
}
