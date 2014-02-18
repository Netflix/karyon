package com.netflix.karyon.governator;

import com.google.inject.Inject;
import com.netflix.karyon.server.bootstrap.HealthCheckHandler;
import com.netflix.karyon.server.bootstrap.HealthCheckInvocationStrategy;

import java.util.concurrent.TimeoutException;

/**
 * @author Nitesh Kant
 */
public class DefaultHealthCheckInvocationStrategy implements HealthCheckInvocationStrategy {

    private final HealthCheckHandler handler;

    @Inject
    public DefaultHealthCheckInvocationStrategy(HealthCheckHandler handler) {
        this.handler = handler;
    }

    @Override
    public int invokeCheck() throws TimeoutException {
        return handler.getStatus();
    }

    @Override
    public HealthCheckHandler getHandler() {
        return handler;
    }
}
