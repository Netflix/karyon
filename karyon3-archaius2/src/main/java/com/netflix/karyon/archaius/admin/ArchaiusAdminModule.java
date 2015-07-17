package com.netflix.karyon.archaius.admin;

import com.netflix.governator.auto.annotations.ConditionalOnModule;
import com.netflix.karyon.admin.AbstractAdminModule;
import com.netflix.karyon.admin.AdminModule;

@ConditionalOnModule(AdminModule.class)
public final class ArchaiusAdminModule extends AbstractAdminModule {
    @Override
    protected void configure() {
        bindAdminResource("props").to(ArchaiusPropResource.class);
        bindAdminResource("props-layers").to(ArchaiusLayerResource.class);
    }
    
    @Override
    public boolean equals(Object obj) {
        return ArchaiusAdminModule.class.equals(obj.getClass());
    }

    @Override
    public int hashCode() {
        return ArchaiusAdminModule.class.hashCode();
    }
}
