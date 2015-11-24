package com.netflix.karyon.admin;

import com.google.inject.AbstractModule;


/**
 * Adding AdminModule to the main injector will enable the admin endpoint
 * 
 * @author elandau
 *
 */
public final class AdminModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(AdminServiceRegistry.class).to(GuiceAdminServiceRegistry.class);
        bind(ResourcesAdminResource.class);
    }
    
    @Override
    public boolean equals(Object obj) {
        return getClass().equals(obj.getClass());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}
