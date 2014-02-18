package com.netflix.karyon.server.bootstrap;

import com.google.common.base.Preconditions;

import javax.annotation.Nullable;

/**
 * A default implementation of {@link KaryonBootstrap} that requires initializing all dependencies eagerly i.e. before
 * creating this class instance and hence {@link #bootstrap()} is a no-op.
 *
 * @author Nitesh Kant
 */
public class DefaultBootstrap implements KaryonBootstrap {

    private final HealthCheckHandler handler;
    private final HealthCheckInvocationStrategy healthCheckInvocationStrategy;
    private final ServiceRegistryClient serviceRegistryClient;

    protected DefaultBootstrap(HealthCheckHandler handler, HealthCheckInvocationStrategy healthCheckInvocationStrategy,
                               ServiceRegistryClient serviceRegistryClient) {
        this.handler = handler;
        this.healthCheckInvocationStrategy = healthCheckInvocationStrategy;
        this.serviceRegistryClient = serviceRegistryClient;
    }

    @Override
    public void bootstrap() {
        // No op, this does not need any explicit bootstrapping per se.
    }

    @Override
    public HealthCheckHandler healthcheckHandler() {
        return handler;
    }

    @Override
    public HealthCheckInvocationStrategy healthCheckInvocationStrategy() {
        return healthCheckInvocationStrategy;
    }

    @Override
    public ServiceRegistryClient serviceRegistryClient() {
        return serviceRegistryClient;
    }

    /**
     * A builder for creating {@link DefaultBootstrap} instances. It has the following defaults:
     * <ul>
     <li>{@link HealthCheckHandler} is defaulted to {@link AlwaysHealthyHealthCheck}</li>
     <li>{@link HealthCheckInvocationStrategy} is defaulted to {@link SyncHealthCheckInvocationStrategy}</li>
     <li>{@link ServiceRegistryClient} is defaulted to {@link NoneServiceRegistryClient}</li>
     </ul>
     */
    public static class Builder {

        private HealthCheckHandler healthCheckHandler = AlwaysHealthyHealthCheck.INSTANCE;
        private HealthCheckInvocationStrategy healthCheckInvocationStrategy =
                new SyncHealthCheckInvocationStrategy(healthCheckHandler);
        private ServiceRegistryClient serviceRegistryClient = new NoneServiceRegistryClient();

        /**
         * Uses {@link DefaultArchaiusInitializer} to initialize archaius. Calls {@link ArchaiusInitializer#initialize()}.<br/>
         * Use {@link #Builder(ArchaiusInitializer)} if you have a custom implementation of {@link ArchaiusInitializer}
         *
         * @param applicationName Application name to be used by archaius.
         * @param environment Environment to be used by archaius.
         */
        public Builder(String applicationName, @Nullable String environment) {
            ArchaiusInitializer initializer = new DefaultArchaiusInitializer(applicationName, environment);
            initializer.initialize();
        }

        /**
         * Uses the passed {@code archaiusInitializer} to initialize archaius. Calls {@link ArchaiusInitializer#initialize()}.<br/>
         *
         * @param archaiusInitializer {@link ArchaiusInitializer} implementation to use.
         */
        public Builder(ArchaiusInitializer archaiusInitializer) {
            Preconditions.checkNotNull(archaiusInitializer, "Initializer can not be null.");
            archaiusInitializer.initialize();
        }

        /**
         * This constructor does not initialize archaius. Use {@link #Builder(ArchaiusInitializer)} or
         * {@link #Builder(String, String)} if you require to initialize archiaus.
         */
        public Builder() {
        }

        public Builder healthCheckHandler(HealthCheckHandler healthCheckHandler) {
            this.healthCheckHandler = healthCheckHandler;
            return this;
        }

        public Builder healthCheckInvocationStrategy(HealthCheckInvocationStrategy healthCheckInvocationStrategy) {
            this.healthCheckInvocationStrategy = healthCheckInvocationStrategy;
            return this;
        }

        public Builder serviceRegistryClient(ServiceRegistryClient serviceRegistryClient) {
            this.serviceRegistryClient = serviceRegistryClient;
            return this;
        }

        public DefaultBootstrap build() {
            return new DefaultBootstrap(healthCheckHandler, healthCheckInvocationStrategy, serviceRegistryClient);
        }
    }
}
