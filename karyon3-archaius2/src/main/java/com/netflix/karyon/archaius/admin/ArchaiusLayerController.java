package com.netflix.karyon.archaius.admin;

import java.util.Arrays;
import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.netflix.archaius.Config;
import com.netflix.archaius.config.CompositeConfig;
import com.netflix.karyon.admin.rest.Controller;

// props-layer
@Singleton
public class ArchaiusLayerController implements Controller {
    private final CompositeConfig config;

    @Inject
    public ArchaiusLayerController(Config config) {
        this.config = (CompositeConfig)config;
    }

    // props-layers (LIST)
    public Collection<String> list() {
        return this.config.getConfigNames();
    }
    
    // props-layers/:id (LIST)
    public Collection<String> find(String layer) {
        return Arrays.asList("layer", layer );
    }

    // props-layer/:id/props
    public Collection<String> listProps(String layer) {
        return null;
    }
    
    // props-layer/:id/props
    public Collection<String> findProps(String layer, String prefix) {
        return null;
    }
}
