package com.netflix.karyon.archaius.admin;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.netflix.archaius.Config;
import com.netflix.archaius.config.CompositeConfig;
import com.netflix.karyon.admin.rest.Controller;

// props
@Singleton
public class ArchaiusPropController implements Controller {
    private final CompositeConfig config;

    @Inject
    public ArchaiusPropController(Config config) {
        this.config = (CompositeConfig)config;
    }

    // props/
    public Map<String, String> list() {
        return find(config);
    }
    
    // props/:id (MAP)
    public Map<String, String> find(String prefix) {
        return find(config.getPrefixedView(prefix));
    }
    
    private Map<String, String> find(Config config) {
        Map<String, String> props = new HashMap<>();
        Iterator<String> iter = config.getKeys();
        while (iter.hasNext()) {
            String key = iter.next();
            props.put(key, (String) config.getString(key, "****"));
        }
        return props;
    }
    
    // props/:id/sources (MAP)
    public LinkedHashMap<String, String> findSources(String key) {
        final LinkedHashMap<String, String> result = new LinkedHashMap<>();
        config.accept(new SourcesVisitor(key, result));
        return result;
    }
}
