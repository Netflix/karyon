package com.netflix.karyon.admin;

import java.util.Map;

import javax.inject.Singleton;

import com.google.inject.Provides;
import com.netflix.karyon.admin.rest.ResourceContainer;
import com.netflix.karyon.admin.rest.DefaultResourceContainer;

/**
 * Adding AdminModule to the main injector will enable the admin endpoint
 * 
 * @author elandau
 *
 */
public class AdminModule extends AbstractAdminModule {
    @Override
    protected void configure() {
        this.bindAdminResource("controllers").to(ResourcesAdminResource.class);
    }
    
    @Singleton
    @Provides
    @AdminServer
    public ResourceContainer getAdminControllerRegistry(@AdminResource Map<String, Object> resources) throws Exception {
        return new DefaultResourceContainer(resources);
    }
    
    @Override
    public boolean equals(Object obj) {
        return AdminModule.class.equals(obj.getClass());
    }

    @Override
    public int hashCode() {
        return AdminModule.class.hashCode();
    }

}
