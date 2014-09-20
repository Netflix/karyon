package com.netflix.karyon;

import com.google.inject.Injector;
import com.netflix.governator.guice.LifecycleInjectorBuilderSuite;
import com.netflix.governator.lifecycle.LifecycleManager;

import java.util.Arrays;

/**
 * @author Nitesh Kant
 */
abstract class AbstractKaryonServer implements KaryonServer {

    protected final LifecycleInjectorBuilderSuite[] suites;
    protected LifecycleManager lifecycleManager;
    protected Injector injector;

    public AbstractKaryonServer(LifecycleInjectorBuilderSuite... suites) {
        this.suites = suites;
    }

    @Override
    public final void start() {
        startWithAdditionalSuites();
    }

    public final void startWithAdditionalSuites(LifecycleInjectorBuilderSuite... additionalSuites) {
        LifecycleInjectorBuilderSuite[] applicableSuites = this.suites;
        if (null != additionalSuites && additionalSuites.length != 0) {
            applicableSuites = Arrays.copyOf(suites, suites.length + additionalSuites.length);
            System.arraycopy(additionalSuites, 0, applicableSuites, suites.length, additionalSuites.length);
        }

        injector = newInjector(applicableSuites);

        startLifecycleManager();
        _start();
    }

    protected abstract void _start();

    @Override
    public void shutdown() {
        if (lifecycleManager != null) {
            lifecycleManager.close();
        }
    }

    @Override
    public void startAndWaitTillShutdown() {
        start();
        waitTillShutdown();
    }

    protected abstract Injector newInjector(LifecycleInjectorBuilderSuite... applicableSuites);

    protected void startLifecycleManager() {
        lifecycleManager = injector.getInstance(LifecycleManager.class);
        try {
            lifecycleManager.start();
        } catch (Exception e) {
            throw new RuntimeException(e); // So that this does not pollute the API.
        }
    }
}
