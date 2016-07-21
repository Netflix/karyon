package netflix.karyon;

import com.google.inject.Injector;
import com.netflix.governator.guice.BootstrapModule;
import com.netflix.governator.guice.LifecycleInjector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

/**
 * @author Nitesh Kant
 * @deprecated 2016-07-20 Karyon2 no longer supported.  See https://github.com/Netflix/karyon/issues/347 for more info
 */
@Deprecated
class MainClassBasedServer extends AbstractKaryonServer {

    private static final Logger logger = LoggerFactory.getLogger(MainClassBasedServer.class);

    private final Class<?> mainClass;

    protected MainClassBasedServer(Class<?> mainClass, BootstrapModule... bootstrapModules) {
        super(bootstrapModules);
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
    protected Injector newInjector(BootstrapModule... applicableBootstrapModules) {
        return LifecycleInjector.bootstrap(mainClass, applicableBootstrapModules);
    }
}
