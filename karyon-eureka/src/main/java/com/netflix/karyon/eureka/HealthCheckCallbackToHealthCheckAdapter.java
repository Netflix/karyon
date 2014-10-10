package com.netflix.karyon.eureka;

import com.netflix.appinfo.HealthCheckCallback;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.karyon.health.HealthCheck;

public class HealthCheckCallbackToHealthCheckAdapter implements HealthCheck {
    private final HealthCheckCallback callback;
    private final InstanceInfo instanceInfo;
    private final String name;
    
    public HealthCheckCallbackToHealthCheckAdapter(HealthCheckCallback callback, InstanceInfo instanceInfo) {
        this.callback = callback;
        this.instanceInfo = instanceInfo;
        this.name = callback.getClass().getName();
    }
    
    @Override
    public Status check() {
        try {
            if (callback.isHealthy()) {
                return Status.ready(this);
            }
            else {
                return Status.notReady(this);
            }
        }
        catch (Exception e) {
            return Status.error(this, e);
        }
    }

    @Override
    public String getName() {
        return name;
    }
}
