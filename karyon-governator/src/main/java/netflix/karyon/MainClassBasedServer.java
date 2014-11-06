package netflix.karyon;

import com.google.inject.Injector;
import com.netflix.governator.guice.LifecycleInjector;
import com.netflix.governator.guice.LifecycleInjectorBuilderSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

/**
 * @author Nitesh Kant
 */
class MainClassBasedServer extends AbstractKaryonServer {

    private static final Logger logger = LoggerFactory.getLogger(MainClassBasedServer.class);

    private final Class<?> mainClass;

    protected MainClassBasedServer(Class<?> mainClass, LifecycleInjectorBuilderSuite... suites) {
        super(suites);
        this.mainClass = mainClass;
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

    @Override
    protected void _start() {
        // No Op.
    }

    @Override
    protected Injector newInjector(LifecycleInjectorBuilderSuite... applicableSuites) {
        return LifecycleInjector.bootstrap(mainClass, applicableSuites);
    }
}
