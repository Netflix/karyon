package com.netflix.karyon.eureka;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.netflix.discovery.DiscoveryClient;

@Singleton
public class EurekaApplicationAdminResource {
    private DiscoveryClient client;

    @Inject
    public EurekaApplicationAdminResource(DiscoveryClient client) {
        this.client = client;
    }
    
    public List<String> get() {
        return client.getApplications().getRegisteredApplications().stream().map((app) -> app.getName()).collect(Collectors.toList());
    }
    
    public List<String> getApplication(String appName) {
        return client.getApplications().getRegisteredApplications(appName).getInstances().stream().map((inst) -> inst.getId()).collect(Collectors.toList());
    }
}
