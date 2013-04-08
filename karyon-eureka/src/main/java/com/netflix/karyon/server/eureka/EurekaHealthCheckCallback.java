/*
 * Copyright 2013 Netflix, Inc.
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */

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
 * @author Nitesh Kant
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
            return isHealthy(healthStatus);
        } catch (TimeoutException e) {
            logger.info("Application health check time out, returning unhealthy. Error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Asserts whether the given status signifies healthy for eureka.
     *
     * @param healthStatus Health status to assert.
     *
     * @return <code>true</code> if the status means healthy.
     */
    protected boolean isHealthy(int healthStatus) {
        return healthStatus >= 200 && healthStatus < 300;
    }
}
