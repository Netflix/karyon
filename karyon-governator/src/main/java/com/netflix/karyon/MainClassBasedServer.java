package com.netflix.karyon;

import com.google.inject.Injector;
import com.netflix.governator.guice.LifecycleInjector;
import com.netflix.governator.guice.LifecycleInjectorBuilderSuite;
import com.netflix.governator.lifecycle.LifecycleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

/**
 * @author Nitesh Kant
 */
class MainClassBasedServer implements KaryonServer {

    private static final Logger logger = LoggerFactory.getLogger(MainClassBasedServer.class);

    private final Class<?> mainClass;
    protected final LifecycleInjectorBuilderSuite[] suites;
    private LifecycleManager lifecycleManager;
    private Injector injector;

    protected MainClassBasedServer(Class<?> mainClass, LifecycleInjectorBuilderSuite... suites) {
        this.mainClass = mainClass;
        this.suites = suites;
    }

    @Override
    public void start() {
        injector = LifecycleInjector.bootstrap(mainClass, suites);
        startLifecycleManager();
    }

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

    @Override
    public void waitTillShutdown() {
        final CountDownLatch shutdownFinished = new CountDownLatch(1);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    shutdown();
                    logger.info("Leaving main loop - shutdown finished.");
                } finally {
                    shutdownFinished.countDown();
                }
            }
        });

        try {
            shutdownFinished.await();
        } catch (InterruptedException e) {
            logger.error("Interrupted while waiting for shutdown.", e);
            Thread.interrupted();
            throw new RuntimeException(e);
        }
    }

    private void startLifecycleManager() {
        lifecycleManager = injector.getInstance(LifecycleManager.class);
        try {
            lifecycleManager.start();
        } catch (Exception e) {
            throw new RuntimeException(e); // So that this does not pollute the API.
        }
    }
}
