package com.netflix.karyon.archaius.admin;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.netflix.archaius.Config;

// props
@Singleton
public class ArchaiusPropResource {
    private final Config config;

    @Inject
    public ArchaiusPropResource(Config config) {
        this.config = config;
    }

    // props/
    public PropsModel get() {
        return get(config, null);
    }
    
    // props/:regex (MAP)
    public PropsModel get(String regex) {
        return get(config, regex);
    }
    
    private PropsModel get(Config config, String regex) {
        Map<String, String> props = new HashMap<>();
        Iterator<String> iter = config.getKeys();
        while (iter.hasNext()) {
            String key = iter.next();
            if (regex == null || key.matches(regex)) {
                props.put(key, (String) config.getString(key, "****"));
            }
        }
        return new PropsModel(props);
    }
    
    // props/:id/sources (MAP)
    public LinkedHashMap<String, String> getSources(String key) {
        final LinkedHashMap<String, String> result = new LinkedHashMap<>();
        config.accept(new SourcesVisitor(key, result));
        return result;
    }
}
