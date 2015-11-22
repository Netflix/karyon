package com.netflix.karyon.eureka.admin;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.appinfo.InstanceInfo.InstanceStatus;
import com.netflix.discovery.EurekaClient;
import com.netflix.karyon.admin.AdminService;

@Singleton
@AdminService(name="eureka-status", index="current")
final class EurekaStatusAdminResource {
    private final EurekaClient client;
    private final InstanceInfo instanceInfo;

    @Inject
    public EurekaStatusAdminResource(EurekaClient client, InstanceInfo instanceInfo) {
        this.client = client;
        this.instanceInfo = instanceInfo;
    }
    
    public static interface DiscoveryStatus {
        InstanceStatus getHealth();
        InstanceInfo getInstanceInfo();
    }
    
    public DiscoveryStatus current() {
        return new DiscoveryStatus() {
            @Override
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
