package com.netflix.karyon.server.eureka;

import com.google.inject.Inject;
import com.netflix.appinfo.HealthCheckCallback;
import com.netflix.config.DynamicBooleanProperty;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.karyon.spi.PropertyNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeoutException;

/**
 * @author Nitesh Kant (nkant@netflix.com)
 */
public class EurekaHealthCheckCallback implements HealthCheckCallback {

    protected static final Logger logger = LoggerFactory.getLogger(EurekaHandler.class);

    private HealthCheckInvocationStrategy healthCheckInvocationStrategy;

    private static final DynamicBooleanProperty UNIFY_HEALTHCHECK_WITH_EUREKA =
            DynamicPropertyFactory.getInstance().getBooleanProperty(PropertyNames.UNIFY_HEALTHCHECK_WITH_EUREKA, true);

    @Inject
    public EurekaHealthCheckCallback(HealthCheckInvocationStrategy healthCheckInvocationStrategy) {
        this.healthCheckInvocationStrategy = healthCheckInvocationStrategy;
    }

    @Override
    public boolean isHealthy() {
        if (!UNIFY_HEALTHCHECK_WITH_EUREKA.get()) {
            // This makes eureka-karyon healthcheck unification dynamic since the healthcheck registration is done once.
            return true;
        }

        try {
            int healthStatus = healthCheckInvocationStrategy.invokeCheck();
            return healthStatus >= 200 && healthStatus < 300;
        } catch (TimeoutException e) {
            logger.info("Application health check time out, returning unhealthy. Error: " + e.getMessage());
            return false;
        }
    }
}
