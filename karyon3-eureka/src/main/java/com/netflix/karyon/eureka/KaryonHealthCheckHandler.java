package com.netflix.karyon.eureka;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.inject.Singleton;

import com.google.inject.Inject;
import com.netflix.appinfo.HealthCheckHandler;
import com.netflix.appinfo.InstanceInfo.InstanceStatus;
import com.netflix.karyon.ApplicationLifecycle;
import com.netflix.karyon.healthcheck.HealthCheck;
import com.netflix.karyon.healthcheck.HealthChecks;

/**
 * Eureka HealthCheckHandler implementation that combines Karyon's HealthCheck with
 * the injector lifecycle to auto-register with Eureka once the injector has been created
 * and the HealthCheck is healthy.
 * 
 * @author elandau
 */
@Singleton
public class KaryonHealthCheckHandler implements HealthCheckHandler {
    private final HealthCheck               healthCheck;
    private final ApplicationLifecycle      applicationLifecycle;
    private final HealthCheckConfiguration  config;
    
    private static class Optional {
        @Inject(optional=true)
        HealthCheck healthCheck;
    }
    
    @Inject
    private KaryonHealthCheckHandler(Optional optional, ApplicationLifecycle applicationStatus, HealthCheckConfiguration config) {
        this(optional.healthCheck != null ? optional.healthCheck : HealthChecks.alwaysHealthy(), applicationStatus, config);
    }
    
    public KaryonHealthCheckHandler(HealthCheck healthCheck, ApplicationLifecycle applicationStatus, HealthCheckConfiguration config) {
        this.healthCheck = healthCheck;
        this.applicationLifecycle = applicationStatus;
        this.config = config;
    }

    @Override
    public InstanceStatus getStatus(InstanceStatus currentStatus) {
        switch (applicationLifecycle.getState()) {
        case Starting:
            try {
                return !healthCheck.check().get(config.getTimeoutInMillis(), TimeUnit.MILLISECONDS).isHealthy()
                        ? InstanceStatus.DOWN
                        : InstanceStatus.STARTING;
            } 
            catch (Exception e) {
                return InstanceStatus.DOWN;
            }
            
        case Started:
            try {
                return healthCheck.check().get(config.getTimeoutInMillis(), TimeUnit.MILLISECONDS).isHealthy() 
                        ? InstanceStatus.UP 
                        : InstanceStatus.DOWN;
            }
            catch (Exception e) {
                return InstanceStatus.DOWN;
            }
            
        case Failed:
            return InstanceStatus.DOWN;
            
        case Stopped:
            return InstanceStatus.OUT_OF_SERVICE;
            
        default:
            return InstanceStatus.UNKNOWN;
        }
    }
}
