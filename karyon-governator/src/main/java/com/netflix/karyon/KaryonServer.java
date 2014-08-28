package com.netflix.karyon;

import java.util.concurrent.CountDownLatch;

import com.google.inject.Injector;
import com.netflix.governator.guice.LifecycleInjector;
import com.netflix.governator.lifecycle.LifecycleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Nitesh Kant
 */
public class KaryonServer {
    private static final Logger logger = LoggerFactory.getLogger(KaryonServer.class);

    private final Class<?> mainClass;

    private LifecycleManager lifecycleManager;
    private Injector injector;

    public KaryonServer(Class<?> mainClass) {
        this.mainClass = mainClass;
    }

    public void start() throws Exception {
        injector = LifecycleInjector.bootstrap(mainClass);
        startLifecycleManager();
    }

    private void startLifecycleManager() throws Exception {
        lifecycleManager = injector.getInstance(LifecycleManager.class);
        lifecycleManager.start();
    }

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
        while (true) {
            try {
                shutdownFinished.await();
                return;
            } catch (InterruptedException e) {
                // IGNORE
            }
        }
    }

    public void shutdown() {
        if (lifecycleManager != null) {
            lifecycleManager.close();
        }
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: " + KaryonServer.class.getCanonicalName() + " <main classs name>");
            System.exit(-1);
        }

        String mainClassName = args[0];
        System.out.println("Using main class: " + mainClassName);

        KaryonServer server = null;
        try {
            server = new KaryonServer(Class.forName(mainClassName));
            server.start();
        } catch (@SuppressWarnings("UnusedCatchParameter") ClassNotFoundException e) {
            System.out.println("Main class: " + mainClassName + "not found.");
            System.exit(-1);
        } catch (Exception e) {
            logger.error("Error while starting karyon server.", e);
            if (server != null) {
                server.shutdown();
            }
            System.exit(-1);
        }
        server.waitTillShutdown();

        // In case we have non-daemon threads running
        System.exit(0);
    }
}
