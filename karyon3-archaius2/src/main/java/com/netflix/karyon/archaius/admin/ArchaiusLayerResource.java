package com.netflix.karyon.archaius.admin;

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
    public PropsModel get(String layer) {
        return new ArchaiusPropResource(config.getConfig(layer)).get();
    }
    
    // props-layer/:id/props/:prefix
    public PropsModel getProps(String layer, String prefix) {
        return new ArchaiusPropResource(config.getConfig(layer)).get(prefix);
    }
}
