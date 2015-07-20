package com.netflix.karyon.archaius.admin;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.netflix.archaius.Config;

@Singleton
// allprops
public class ArchaiusAllpropsResource {
    private final Config config;

    @Inject
    public ArchaiusAllpropsResource(Config config) {
        this.config = config;
    }

    // allprops/
    public PropsModel list() {
        Map<String, String> props = new HashMap<>();
        Iterator<String> iter = config.getKeys();
        while (iter.hasNext()) {
            String key = iter.next();
            props.put(key, (String) config.getString(key, "****"));
        }
        
        return new PropsModel(props);
    }
}
