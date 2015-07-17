package com.netflix.karyon.admin;

import java.util.Map;

import javax.inject.Singleton;

import com.google.inject.Provides;
import com.netflix.karyon.admin.rest.Controller;
import com.netflix.karyon.admin.rest.ControllerRegistry;
import com.netflix.karyon.admin.rest.DefaultControllerRegistry;

/**
 * Adding AdminModule to the main injector will enable the admin endpoint
 * 
 * @author elandau
 *
 */
public class AdminModule extends AbstractAdminModule {
    @Override
    protected void configure() {
        this.bindAdminController("controllers").to(ControllersAdminController.class);
    }
    
    @Singleton
    @Provides
    @Admin
    public ControllerRegistry getAdminControllerRegistry(@Admin Map<String, Controller> controllers) throws Exception {
        return new DefaultControllerRegistry(controllers);
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
