package com.netflix.karyon.server.eureka;

import com.google.inject.Inject;
import com.netflix.karyon.spi.HealthCheckHandler;

import java.util.concurrent.TimeoutException;

/**
 * An implementation of {@link HealthCheckInvocationStrategy} that synchronously calls the underlying
 * {@link HealthCheckHandler}.
 *
 * @author Nitesh Kant (nkant@netflix.com)
 */
@SuppressWarnings("unused")
public class SyncHealthCheckInvocationStrategy implements HealthCheckInvocationStrategy {

    private HealthCheckHandler handler;

    @Inject
    public SyncHealthCheckInvocationStrategy(HealthCheckHandler handler) {
        this.handler = handler;
    }

    @Override
    public int invokeCheck() throws TimeoutException {
        return handler.checkHealth();
    }
}
