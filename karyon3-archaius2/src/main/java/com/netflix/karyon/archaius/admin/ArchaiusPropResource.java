package com.netflix.karyon.archaius.admin;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.netflix.archaius.Config;
import com.netflix.archaius.visitor.PropertyOverrideVisitor;

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
                try {
                    props.put(key, (String) config.getString(key, "****"));
                }
                catch (Exception e) {
                    props.put(key, "*** ERROR PARSING PROPERTY : " + e.getMessage());
                }
            }
        }
        return new PropsModel(props);
    }
    
    // props/:id/sources (MAP)
    public LinkedHashMap<String, String> getSources(String key) {
        return config.accept(new PropertyOverrideVisitor(key));
    }
}
