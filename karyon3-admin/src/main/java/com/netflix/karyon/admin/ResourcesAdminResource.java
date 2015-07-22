package com.netflix.karyon.admin;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.netflix.karyon.admin.rest.ResourceContainer;

@Singleton
public class ResourcesAdminResource {
    private ResourceContainer registry;

    @Inject
    public ResourcesAdminResource(@AdminServer ResourceContainer registry) {
        this.registry = registry;
    }
    
    public Set<String> get() {
        return registry.getNames();
    }
}
