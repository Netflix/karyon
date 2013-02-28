package com.netflix.karyon.server.eureka;

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
}
