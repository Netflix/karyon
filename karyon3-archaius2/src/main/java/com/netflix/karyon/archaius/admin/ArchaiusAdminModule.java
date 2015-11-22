package com.netflix.karyon.archaius.admin;

import com.google.inject.AbstractModule;

public class ArchaiusAdminModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(ArchaiusResource.class);
        bind(MetaAdminResource.class);
    }
}
