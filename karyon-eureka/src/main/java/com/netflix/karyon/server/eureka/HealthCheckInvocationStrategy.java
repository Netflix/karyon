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

import com.google.inject.ImplementedBy;
import com.netflix.karyon.spi.HealthCheckHandler;

import java.util.concurrent.TimeoutException;

/**
 * A strategy to make application specific healthchecks. Since, the application health checks can be poorly implemented
 * and hence take a long time to complete, in some cases, it is wise to have an SLA around the health check response
 * times. <p/>
 * The default health check strategy {@link AsyncHealthCheckInvocationStrategy} can be overridden by providing the
 * strategy class name in the property {@link com.netflix.karyon.spi.PropertyNames#HEALTH_CHECK_STRATEGY} which should
 * implement this interface. <br/>
 * There is a 1:1 mapping between a strategy instance and a {@link HealthCheckHandler} instance and hence it is assumed
 * that the strategy already knows about the handler instance.
 *
 * @author Nitesh Kant
 */
@ImplementedBy(AsyncHealthCheckInvocationStrategy.class)
public interface HealthCheckInvocationStrategy {

    /**
     * Invokes the handler associated with this strategy and returns the response. This method may block waiting for results. <br/>
     * If this strategy supports timeouts, this call must not wait more than the timeout value.
     *
     * @return The health check result.
     *
     * @throws TimeoutException If the healthcheck did not return after the stipulated time (governed entirely by this
     * strategy implementation)
     */
    int invokeCheck() throws TimeoutException;

    /**
     * Returns the instance of {@link HealthCheckHandler} associated with this strategy.
     *
     * @return The instance of {@link HealthCheckHandler} associated with this strategy.
     */
    HealthCheckHandler getHandler();
}
