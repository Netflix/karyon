package com.netflix.karyon.experimental;

import com.google.inject.Injector;
import com.netflix.governator.guice.LifecycleInjector;
import com.netflix.governator.lifecycle.LifecycleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Tomasz Bak
 */
public class ExpKyronServer {

    private static final Logger logger = LoggerFactory.getLogger(ExpKyronServer.class);

    public void waitTillShutdown() throws InterruptedException {
        // TODO: shutdown hook
        while (true) {
            Thread.sleep(10000);
        }
    }

    protected static void bootstrap(Class<? extends ExpKyronServer> serverBootstrapClass) throws InterruptedException {
        Injector injector = LifecycleInjector.bootstrap(serverBootstrapClass);
        LifecycleManager lifecycleManager = injector.getInstance(LifecycleManager.class);

        try {
            lifecycleManager.start();
        } catch (Exception e) {
            logger.error("Cannot start container lifecycle manager", e);
        }
        ExpKyronServer bootstrap = injector.getInstance(serverBootstrapClass);
        bootstrap.waitTillShutdown();
    }
}
