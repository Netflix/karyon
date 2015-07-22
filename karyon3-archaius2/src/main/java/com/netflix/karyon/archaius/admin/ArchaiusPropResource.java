package com.netflix.karyon.archaius.admin;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.netflix.archaius.Config;
import com.netflix.archaius.config.CompositeConfig;

// props
@Singleton
public class ArchaiusPropResource {
    private final CompositeConfig config;

    @Inject
    public ArchaiusPropResource(Config config) {
        this.config = (CompositeConfig)config;
    }

    // props/
    public Map<String, String> get() {
        return get(config, null);
    }
    
    // props/:regex (MAP)
    public Map<String, String> get(String regex) {
        return get(config, regex);
    }
    
    private Map<String, String> get(Config config, String regex) {
        Map<String, String> props = new HashMap<>();
        Iterator<String> iter = config.getKeys();
        while (iter.hasNext()) {
            String key = iter.next();
            if (regex == null || key.matches(regex)) {
                props.put(key, (String) config.getString(key, "****"));
            }
        }
        return props;
    }
    
    // props/:id/sources (MAP)
    public LinkedHashMap<String, String> getSources(String key) {
        final LinkedHashMap<String, String> result = new LinkedHashMap<>();
        config.accept(new SourcesVisitor(key, result));
        return result;
    }
}
