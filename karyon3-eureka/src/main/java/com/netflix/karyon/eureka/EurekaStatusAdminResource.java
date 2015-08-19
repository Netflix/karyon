package com.netflix.karyon.eureka;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.appinfo.InstanceInfo.InstanceStatus;
import com.netflix.discovery.DiscoveryClient;

@Singleton
public class EurekaStatusAdminResource {
    private final DiscoveryClient client;
    private final InstanceInfo instanceInfo;

    @Inject
    public EurekaStatusAdminResource(DiscoveryClient client, InstanceInfo instanceInfo) {
        this.client = client;
        this.instanceInfo = instanceInfo;
    }
    
    public static interface DiscoveryStatus {
        public InstanceStatus getHealth();
        public String getRegion();
    }
    
    public DiscoveryStatus get() {
        return new DiscoveryStatus() {
            @Override
            public String getRegion() {
                return client.getRegion();
            }
            
            public InstanceInfo getInstanceInfo() {
                return instanceInfo;
            }
            
            @Override
            public InstanceInfo.InstanceStatus getHealth() {
                return client.getHealthCheckHandler().getStatus(InstanceStatus.UNKNOWN);
            }
        };
    }
}
