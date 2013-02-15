package com.netflix.karyon.server.eureka;

import com.google.inject.Inject;
import com.netflix.appinfo.HealthCheckCallback;
import com.netflix.karyon.spi.HealthCheckHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Nitesh Kant (nkant@netflix.com)
 */
public class EurekaHealthCheckCallback implements HealthCheckCallback {

    protected static final Logger logger = LoggerFactory.getLogger(EurekaHandler.class);

    private HealthCheckHandler healthCheckHandler;

    @Inject
    public EurekaHealthCheckCallback(HealthCheckHandler healthCheckHandler) {
        if (null != healthCheckHandler) {
            logger.info(String.format("Application health check handler to be used by karyon: %s",
                    healthCheckHandler.getClass().getName()));
            this.healthCheckHandler = healthCheckHandler;
        }
    }

    @Override
    public boolean isHealthy() {
        if (null != healthCheckHandler) {
            int healthStatus = healthCheckHandler.checkHealth();
            return healthStatus > 200 && healthStatus < 300;
        }
        return true;
    }
}
