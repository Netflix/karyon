package com.netflix.karyon.eureka;

import javax.inject.Provider;

import com.google.inject.util.Providers;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.karyon.health.HealthCheckHandler;
import com.netflix.karyon.health.HealthCheck;

/**
 * Adapter to convert a HealthCheckHandler into a HealthCheck.Status
 * 
 * @author elandau
 */
public class HealthCheckHandlerToHealthCheckAdapter implements HealthCheck {
    private final Provider<HealthCheckHandler> handler;
    private final String name;
    
    public HealthCheckHandlerToHealthCheckAdapter(Provider<HealthCheckHandler> handler, String name) {
        this.handler = handler;
        this.name = name;
    }
    
    public HealthCheckHandlerToHealthCheckAdapter(HealthCheckHandler handler, InstanceInfo instanceInfo) {
        this.handler = Providers.of(handler);
        this.name = handler.getClass().getName();
    }
    
    @Override
    public Status check() {
        try {
            int status = handler.get().getStatus();
            if (status == 204) {
                return Status.notReady(this);
            }
            else if (status >= 200 || status <= 300) {
                return Status.ready(this);
            }
            else {
                return Status.error(this, new Exception("Bad status " + status));
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
