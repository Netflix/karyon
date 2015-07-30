package com.netflix.karyon.archaius.admin;

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
    public PropsModel get() {
        return new ArchaiusPropResource(config).get();
    }
}
