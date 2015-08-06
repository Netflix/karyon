package com.netflix.karyon.eureka;

import javax.inject.Singleton;

import com.google.inject.Inject;
import com.netflix.appinfo.HealthCheckHandler;
import com.netflix.appinfo.InstanceInfo.InstanceStatus;
import com.netflix.governator.LifecycleListener;
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
public class KaryonHealthCheckHandler implements HealthCheckHandler, LifecycleListener {

    public static enum State {
        Starting,
        Started,
        Stopped,
        Failed
    }
    
    private State state = State.Starting;
    private final HealthCheck healthCheck;
    
    private static class Optional {
        @Inject(optional=true)
        HealthCheck healthCheck;
    }
    
    @Inject
    private KaryonHealthCheckHandler(Optional optional) {
        this(optional.healthCheck != null ? optional.healthCheck : HealthChecks.alwaysHealthy());
    }
    
    public KaryonHealthCheckHandler(HealthCheck healthCheck) {
        this.healthCheck = healthCheck;
    }
    
    @Override
    public InstanceStatus getStatus(InstanceStatus currentStatus) {
        switch (state) {
        case Starting:
            if (!healthCheck.check().isHealthy()) {
                return InstanceStatus.DOWN;
            }
            return InstanceStatus.STARTING;
            
        case Started:
            return healthCheck.check().isHealthy() ? InstanceStatus.UP : InstanceStatus.DOWN;
            
        case Stopped:
        case Failed:
            return InstanceStatus.DOWN;
            
        default:
            return InstanceStatus.UNKNOWN;
        }
    }

    @Override
    public void onStarted() {
        state = State.Started;
    }

    @Override
    public void onStopped() {
        state = State.Stopped;
    }

    @Override
    public void onStartFailed(Throwable t) {
        state = State.Failed;
    }
}
