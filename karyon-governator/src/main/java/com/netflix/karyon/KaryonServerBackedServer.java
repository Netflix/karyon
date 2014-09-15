package com.netflix.karyon;

import com.netflix.governator.guice.LifecycleInjectorBuilderSuite;

/**
 * An implementation of {@link KaryonServer} which wraps an existing {@link KaryonServer}.
 *
 * @author Nitesh Kant
 */
class KaryonServerBackedServer implements KaryonServer {

    private final AbstractKaryonServer delegate;
    private final LifecycleInjectorBuilderSuite[] suites;

    KaryonServerBackedServer(AbstractKaryonServer delegate, LifecycleInjectorBuilderSuite... suites) {
        this.delegate = delegate;
        this.suites = suites;
    }

    @Override
    public void start() {
        delegate.startWithAdditionalSuites(suites);
    }

    @Override
    public void shutdown() {
        delegate.shutdown();
    }

    @Override
    public void waitTillShutdown() {
        delegate.waitTillShutdown();
    }

    @Override
    public void startAndWaitTillShutdown() {
        start();
        waitTillShutdown();
    }

}
