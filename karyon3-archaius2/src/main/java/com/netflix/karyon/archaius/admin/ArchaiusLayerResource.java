package com.netflix.karyon.archaius.admin;

import java.util.Arrays;
import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.netflix.archaius.Config;
import com.netflix.archaius.config.CompositeConfig;

// props-layer
@Singleton
public class ArchaiusLayerResource {
    private final CompositeConfig config;

    @Inject
    public ArchaiusLayerResource(Config config) {
        this.config = (CompositeConfig)config;
    }

    // props-layers (LIST)
    public Collection<String> get() {
        return this.config.getConfigNames();
    }
    
    // props-layers/:id (LIST)
    public Collection<String> get(String layer) {
        return Arrays.asList("layer", layer );
    }

    // props-layer/:id/props
    public Collection<String> getProps(String layer) {
        return null;
    }
    
    // props-layer/:id/props
    public Collection<String> getProps(String layer, String prefix) {
        return null;
    }
}
