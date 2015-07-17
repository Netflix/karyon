package com.netflix.karyon.admin;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.netflix.karyon.admin.rest.Controller;
import com.netflix.karyon.admin.rest.ControllerRegistry;

@Singleton
public class ControllersAdminController implements Controller {
    private ControllerRegistry registry;

    @Inject
    public ControllersAdminController(@Admin ControllerRegistry registry) {
        this.registry = registry;
    }
    
    public Set<String> list() {
        return registry.getNames();
    }
}
