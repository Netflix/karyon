package com.netflix.karyon.archaius.admin;

import com.google.inject.AbstractModule;
import com.netflix.karyon.admin.AdminModule;
import com.netflix.karyon.conditional.ConditionalOnModule;

@ConditionalOnModule(AdminModule.class)
public final class ArchaiusAdminModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(ArchaiusResource.class);
    }
}
