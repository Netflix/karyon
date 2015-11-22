package com.netflix.karyon.eureka.admin;

import com.google.inject.AbstractModule;
import com.netflix.discovery.EurekaClient;

public class EurekaAdminModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(EurekaApplicationAdminResource.class);
        bind(EurekaStatusAdminResource.class);
        
        requireBinding(EurekaClient.class);
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
