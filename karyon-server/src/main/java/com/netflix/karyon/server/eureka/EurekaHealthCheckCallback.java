package com.netflix.karyon.server.eureka;

import com.google.inject.Inject;
import com.netflix.appinfo.HealthCheckCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeoutException;

/**
 * @author Nitesh Kant (nkant@netflix.com)
 */
public class EurekaHealthCheckCallback implements HealthCheckCallback {

    protected static final Logger logger = LoggerFactory.getLogger(EurekaHandler.class);

    private HealthCheckInvocationStrategy healthCheckInvocationStrategy;

    @Inject
    public EurekaHealthCheckCallback(HealthCheckInvocationStrategy healthCheckInvocationStrategy) {
        this.healthCheckInvocationStrategy = healthCheckInvocationStrategy;
    }

    @Override
    public boolean isHealthy() {
        try {
            int healthStatus = healthCheckInvocationStrategy.invokeCheck();
            return healthStatus >=200 && healthStatus < 300;
        } catch (TimeoutException e) {
            logger.info("Application health check time out, returning unhealthy. Error: " + e.getMessage());
            return false;
        }
    }
}
