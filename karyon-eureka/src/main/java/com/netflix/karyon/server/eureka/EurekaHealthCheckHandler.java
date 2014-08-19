package com.netflix.karyon.server.eureka;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.karyon.spi.HealthCheckHandler;

import javax.inject.Inject;

/**
 * @author Nitesh Kant
 */
public class EurekaHealthCheckHandler implements com.netflix.appinfo.HealthCheckHandler {

    private final HealthCheckHandler healthCheckHandler;
    private final EurekaKaryonStatusBridge eurekaKaryonStatusBridge;

    @Inject
    public EurekaHealthCheckHandler(HealthCheckHandler healthCheckHandler,
                                    EurekaKaryonStatusBridge eurekaKaryonStatusBridge) {
        this.healthCheckHandler = healthCheckHandler;
        this.eurekaKaryonStatusBridge = eurekaKaryonStatusBridge;
    }

    @Override
    public InstanceInfo.InstanceStatus getStatus(InstanceInfo.InstanceStatus currentStatus) {
        int healthStatus = healthCheckHandler.getStatus();
        return eurekaKaryonStatusBridge.interpretKaryonStatus(healthStatus);
    }
}