package com.netflix.karyon.admin;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.netflix.archaius.Config;

@Singleton
@AdminService(name="meta", index="list")
public class MetaAdminResource {
    private Map<String, String> prop = new HashMap<>();
    
    @Inject
    public MetaAdminResource(Config config) {
        Iterator<String> iter = config.getKeys();
        
        while (iter.hasNext()) {
            String key = iter.next();
            if (key.startsWith("karyon.")) {
                prop.put(key, config.getString(key));
            }
        }
    }
    
    public Map<String, String> list() {
        return prop;
    }
}
