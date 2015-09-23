package com.google.inject.servlet;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;

/**
 * This class is a major hack to gain access to package private ServletDefinition and FilterDefinition
 * from Guice.
 * 
 * @author elandau
 *
 */
@Singleton
public class ServletInfo {
    private static final Logger LOG = LoggerFactory.getLogger(ServletInfo.class);
    
    final Map<String, String> servlets = new HashMap<>();
    final Map<String, String> filters = new HashMap<>();
    
    @Inject
    public ServletInfo(Injector injector) {
        // Extract pattern to servlet key information from ServletDefinition configure using ServletModule
        List<Binding<ServletDefinition>> servletBindings = injector.findBindingsByType(TypeLiteral.get(ServletDefinition.class));
        for (Binding<ServletDefinition> binding : servletBindings) {
            ServletDefinition def = binding.getProvider().get();
            servlets.put(def.getPattern(), def.getKey());
        }
        
        // Extract pattern to filter key information from FilterDefinition configure using ServletModule
        List<Binding<FilterDefinition>> filterBindings = injector.findBindingsByType(TypeLiteral.get(FilterDefinition.class));
        for (Binding<FilterDefinition> binding : filterBindings) {
            FilterDefinition def = binding.getProvider().get();
            
            try {
                Field patternField = def.getClass().getDeclaredField("pattern");
                patternField.setAccessible(true);
                
                Field keyField = def.getClass().getDeclaredField("filterKey");
                keyField.setAccessible(true);
                
                filters.put(patternField.get(def).toString(), keyField.get(def).toString());
            } catch (Exception e) {
                LOG.debug("Unable to get details for {}", def.toString(), e);
            }
        }
        
    }
    
    /**
     * Map of pattern to servlet key information configured on the Guice ServletModule
     * @return
     */
    public Map<String, String> getServlets() {
        return servlets;
    }
    
    /**
     * Map of pattern to fitler key information configured on the Guice ServletModule
     * @return
     */
    public Map<String, String> getFilters() {
        return filters;
    }
}
