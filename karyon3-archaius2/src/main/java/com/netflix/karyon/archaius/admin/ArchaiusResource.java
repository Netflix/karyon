package com.netflix.karyon.archaius.admin;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.netflix.archaius.Config;
import com.netflix.archaius.config.CompositeConfig;
import com.netflix.archaius.visitor.PropertyOverrideVisitor;
import com.netflix.karyon.admin.AdminService;

@Singleton
@AdminService(name="props", index="getAllProperties")
final class ArchaiusResource {
    private final CompositeConfig config;

    @Inject
    public ArchaiusResource(Config config) {
        this.config = (CompositeConfig)config;
    }

    public PropsModel getAllProperties() {
        return get(config, null);
    }
    
    public PropsModel findProperty(PropertyRequest request) {
        return get(config, request.getRegex());
    }
    
    public Collection<String> getLayerNames() {
        return this.config.getConfigNames();
    }
    
    public PropsModel getLayerProperties(PropertyRequest request) {
        return get(config.getConfig(request.getLayerName()), null);
    }
    
    public LinkedHashMap<String, String> getPropertySources(PropertyRequest request) {
        return config.accept(new PropertyOverrideVisitor(request.getKey()));
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
    
}
