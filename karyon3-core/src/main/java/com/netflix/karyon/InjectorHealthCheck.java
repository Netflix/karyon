package com.netflix.karyon;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.netflix.governator.DefaultLifecycleListener;
import com.netflix.governator.LifecycleManager;
import com.netflix.karyon.healthcheck.HealthCheck;
import com.netflix.karyon.healthcheck.HealthStatus;
import com.netflix.karyon.healthcheck.HealthStatuses;

@Singleton
public class InjectorHealthCheck extends DefaultLifecycleListener implements HealthCheck {
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss z");

    private final LifecycleManager manager;
    private final Map<String, Object> attributes = new HashMap<>();
    
    @Inject
    public InjectorHealthCheck(LifecycleManager manager) {
        this.manager = manager;
        
        attributes.put("startTime", DATE_TIME_FORMAT.format(ZonedDateTime.now()));
    }
    
    @Override
    public void onStopped() {
        attributes.put("stoppedTime", DATE_TIME_FORMAT.format(ZonedDateTime.now()));
    }

    @Override
    public void onStarted() {
        attributes.put("startedTime", DATE_TIME_FORMAT.format(ZonedDateTime.now()));
    }

    @Override
    public void onStartFailed(Throwable t) {
        attributes.put("failedTime", DATE_TIME_FORMAT.format(ZonedDateTime.now()));
    }

    @Override
    public HealthStatus check() {
        switch (manager.getState()) {
        case Starting:  
            return HealthStatuses.starting(attributes);
        case Started:
            return HealthStatuses.healthy(attributes);
        case Stopped:
        case Done:
            return HealthStatuses.stopped(attributes);
        default:
            return HealthStatuses.unhealthy(new Exception("Unknown health state"));
        }
    }
}
