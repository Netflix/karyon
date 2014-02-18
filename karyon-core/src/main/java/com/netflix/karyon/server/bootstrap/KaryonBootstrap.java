package com.netflix.karyon.server.bootstrap;

import javax.annotation.Nullable;

/**
 * A bootstrap interface to define any configuration required to start a karyon server. <br/>
 *
 * <h2>Idempotency</h2>
 *
 * All methods in any implementation must be idempotent.
 *
 * @author Nitesh Kant
 */
public interface KaryonBootstrap {

    /**
     * Starts this bootstrap. Calling this method multiple times, should yield the same result.
     */
    void bootstrap();

    /**
     * Returns the {@link HealthCheckHandler} implementation for this server. This method must be idempotent i.e. each
     * invocation should return the same instance of the handler.
     *
     * @return The {@link HealthCheckHandler} implementation for this server. {@code null} if none specified.
     *
     * @throws IllegalStateException If the health check handler is not yet created, typically via a call to
     * {@link #bootstrap()}
     */
    @Nullable HealthCheckHandler healthcheckHandler();

    /**
     * Returns the {@link HealthCheckInvocationStrategy} implementation for this server. This method must be idempotent i.e. each
     * invocation should return the same instance of the strategy.
     *
     * @return The {@link HealthCheckInvocationStrategy} implementation for this server.  {@code null} if none specified.
     *
     * @throws IllegalStateException If the health check strategy is not yet created, typically via a call to
     * {@link #bootstrap()}
     */
    @Nullable HealthCheckInvocationStrategy healthCheckInvocationStrategy();

    /**
     * The {@link ServiceRegistryClient} implementation for this server. This method must be idempotent i.e. each
     * invocation should return the same instance of the client.
     *
     * @return The {@link ServiceRegistryClient} implementation for this server. {@code null} if none specified.
     *
     * @throws IllegalStateException If the client is not yet created, typically via a call to {@link #bootstrap()}
     */
    @Nullable ServiceRegistryClient serviceRegistryClient();
}
