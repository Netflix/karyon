package com.netflix.karyon.admin;

import java.util.Set;
import java.util.TreeSet;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
@AdminService(name="resources", index="list")
public class ResourcesAdminResource {
    private final AdminServiceRegistry services;

    @Inject
    public ResourcesAdminResource(AdminServiceRegistry services) {
        this.services = services;
    }
    
    public Set<String> list() {
        return new TreeSet<>(services.getServiceNames());
    }
}
