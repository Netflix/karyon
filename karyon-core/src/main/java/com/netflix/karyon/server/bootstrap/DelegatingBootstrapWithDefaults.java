package com.netflix.karyon.server.bootstrap;

import com.google.common.base.Preconditions;

/**
 * An implementation of {@link KaryonBootstrap} that uses a delegate implementation of {@link KaryonBootstrap} but adds
 * the defaults if the underlying delegate returns {@code null}. <br/>
 * The defaults are as provided by {@link DefaultBootstrap.Builder}
 *
 * @author Nitesh Kant
 */
public class DelegatingBootstrapWithDefaults implements KaryonBootstrap {

    public static final DefaultBootstrap DEFAULT_WHEN_NULL = new DefaultBootstrap.Builder().build();
    private final KaryonBootstrap delegate;

    public DelegatingBootstrapWithDefaults(KaryonBootstrap delegate) {
        this.delegate = delegate;
        Preconditions.checkNotNull(delegate, "Bootstrap delegate can not be null.");
    }

    @Override
    public void bootstrap() {
        delegate.bootstrap();
    }

    @Override
    public HealthCheckHandler healthcheckHandler() {
        HealthCheckHandler healthCheckHandler = delegate.healthcheckHandler();
        return null == healthCheckHandler ? DEFAULT_WHEN_NULL.healthcheckHandler() : healthCheckHandler;
    }

    @Override
    public HealthCheckInvocationStrategy healthCheckInvocationStrategy() {
        HealthCheckInvocationStrategy strategy = delegate.healthCheckInvocationStrategy();
        return null == strategy ? DEFAULT_WHEN_NULL.healthCheckInvocationStrategy() : strategy;
    }

    @Override
    public ServiceRegistryClient serviceRegistryClient() {
        ServiceRegistryClient serviceRegistryClient = delegate.serviceRegistryClient();
        return null == serviceRegistryClient ? DEFAULT_WHEN_NULL.serviceRegistryClient() : serviceRegistryClient;
    }
}
