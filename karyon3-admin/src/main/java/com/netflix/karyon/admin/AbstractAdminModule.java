package com.netflix.karyon.admin;

import com.google.inject.AbstractModule;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.multibindings.MapBinder;
import com.netflix.karyon.admin.rest.Controller;

/**
 * To be used by libraries wishing to add an admin controller
 * 
 * @author elandau
 *
 */
public abstract class AbstractAdminModule extends AbstractModule {
    protected LinkedBindingBuilder<Controller> bindAdminController(String name) {
        return MapBinder.newMapBinder(binder(), String.class, Controller.class, Admin.class).addBinding(name);
    }
}
