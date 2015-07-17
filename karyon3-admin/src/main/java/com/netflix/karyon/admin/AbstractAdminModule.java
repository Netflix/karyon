package com.netflix.karyon.admin;

import com.google.inject.AbstractModule;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.multibindings.MapBinder;

/**
 * To be used by libraries wishing to add an admin controller
 * 
 * @author elandau
 *
 */
public abstract class AbstractAdminModule extends AbstractModule {
    protected LinkedBindingBuilder<Object> bindAdminResource(String name) {
        return MapBinder.newMapBinder(binder(), String.class, Object.class, AdminResource.class).addBinding(name);
    }
}
