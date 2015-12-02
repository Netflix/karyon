package com.netflix.karyon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.spi.Elements;
import com.netflix.karyon.annotations.Priority;
import com.netflix.karyon.spi.AutoBinder;
import com.netflix.karyon.spi.ModuleListTransformer;

/**
 * 
 */
public class AutoBindingModuleTransformer implements ModuleListTransformer {
    final static Comparator<AutoBinder> byPriority = new Comparator<AutoBinder>() {
        @Override
        public int compare(AutoBinder o1, AutoBinder o2) {
            int p1 = o1.getClass().isAnnotationPresent(Priority.class) ? o1.getClass().getAnnotation(Priority.class).value() : 0;
            int p2 = o2.getClass().isAnnotationPresent(Priority.class) ? o2.getClass().getAnnotation(Priority.class).value() : 0;
            return p2 - p1;
        }
    };
    
    private Collection<AutoBinder> prioritizedAutoBinders;
    
    public AutoBindingModuleTransformer(Collection<AutoBinder> autoBinders) {
        this.prioritizedAutoBinders = autoBinders.stream().sorted(byPriority).collect(Collectors.toList());
        
    }
    
    @Override
    public List<Module> transform(final List<Module> modules) {
        List<Module> finalModules = new ArrayList<>();
        finalModules.addAll(modules);
        
        Set<Key<?>> existingBindings = ElementsEx.getAllUnboundKeys(Elements.getElements(finalModules));
        Set<Key<?>> previousBindings;
        
        do {
            previousBindings = existingBindings;
            for (Key<?> key : existingBindings) {
                for (AutoBinder factory : prioritizedAutoBinders) {
                    if (factory.matches(key)) {
                        finalModules.add(factory.getModuleForKey(key));
                        break;
                    }
                }
            }
            existingBindings = ElementsEx.getAllUnboundKeys(Elements.getElements(finalModules));
        } while (previousBindings.size() != existingBindings.size());   // Repeat until we have no more new bindings
        
        return finalModules;
    }
}
