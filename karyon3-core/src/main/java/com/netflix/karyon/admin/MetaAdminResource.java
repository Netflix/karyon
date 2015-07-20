package com.netflix.karyon.admin;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.netflix.archaius.Config;

@Singleton
public class MetaAdminResource {
    private Map<String, String> prop = new HashMap<>();
    
    @Inject
    public MetaAdminResource(Config config) {
        Iterator<String> iter = config.getKeys();
        
        while (iter.hasNext()) {
            String key = iter.next();
            if (key.startsWith("@")) {
                prop.put(key.substring(1), config.getString(key));
            }
        }
    }
    
    public Map<String, String> list() {
        return prop;
    }
}
