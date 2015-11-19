package com.netflix.karyon.eureka;

import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import com.google.inject.Inject;
import com.netflix.appinfo.HealthCheckHandler;
import com.netflix.appinfo.InstanceInfo.InstanceStatus;
import com.netflix.karyon.api.health.HealthCheck;

/**
 * Eureka HealthCheckHandler implementation that combines Karyon's HealthCheck with
 * the injector lifecycle to auto-register with Eureka once the injector has been created
 * and the HealthCheck is healthy.
 * 
 * @author elandau
 */
@Singleton
public class KaryonHealthCheckHandler implements HealthCheckHandler {
    private final HealthCheck        healthCheck;
    private HealthCheckConfiguration config;
    
    @Inject
    public KaryonHealthCheckHandler(HealthCheck healthCheck, HealthCheckConfiguration config) {
        this.healthCheck = healthCheck;
        this.config = config;
    }

    @Override
    public InstanceStatus getStatus(InstanceStatus currentStatus) {
        try {
            switch (healthCheck.check().get(config.getTimeoutInMillis(), TimeUnit.MILLISECONDS).getState()) {
            case Starting:
                return InstanceStatus.STARTING;
                
            case Healthy:
                return InstanceStatus.UP;
                
            case Unhealthy:
                return InstanceStatus.DOWN;
                
            case OutOfService:
                return InstanceStatus.OUT_OF_SERVICE;
                
            default:
                return InstanceStatus.UNKNOWN;
            }
        } catch (Exception e) {
            return InstanceStatus.DOWN;
        }
    }
}
