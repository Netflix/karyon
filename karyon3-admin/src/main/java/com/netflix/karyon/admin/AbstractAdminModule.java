package com.netflix.karyon.admin;

import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.multibindings.MapBinder;
import com.netflix.governator.DefaultModule;

/**
 * To be used by libraries wishing to add an admin controller
 * 
 * @author elandau
 *
 */
public abstract class AbstractAdminModule extends DefaultModule {
    protected LinkedBindingBuilder<Object> bindAdminResource(String name) {
        return MapBinder.newMapBinder(binder(), String.class, Object.class, AdminResource.class).addBinding(name);
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
