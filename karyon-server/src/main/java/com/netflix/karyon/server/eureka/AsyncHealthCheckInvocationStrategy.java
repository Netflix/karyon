package com.netflix.karyon.server.eureka;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.governator.annotations.Configuration;
import com.netflix.karyon.spi.HealthCheckHandler;
import com.netflix.karyon.spi.PropertyNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * An implementation of {@link HealthCheckInvocationStrategy} that calls the underlying {@link HealthCheckHandler}
 * asynchronously. If the underlying handler takes more time than configured in the property
 * {@link com.netflix.karyon.spi.PropertyNames#HEALTH_CHECK_TIMEOUT_MILLIS}, this handler will throw a {@link TimeoutException}.
 * The timeout is retrieved using archaius's dynamic properties and hence can be changed at runtime. The default timeout
 * value is as configured by the property{@link PropertyNames#HEALTH_CHECK_TIMEOUT_DEFAULT_MILLIS} milliseconds. <p/>
 *
 * <h3>Threading model</h3>
 *
 * @author Nitesh Kant (nkant@netflix.com)
 */
@Singleton
public class AsyncHealthCheckInvocationStrategy implements HealthCheckInvocationStrategy {

    protected static final Logger logger = LoggerFactory.getLogger(AsyncHealthCheckInvocationStrategy.class);

    @Configuration(
            value = PropertyNames.HEALTH_CHECK_TIMEOUT_DEFAULT_MILLIS,
            documentation = "Default timeout value for healthchecks in milliseconds."
    )
    private int HEALTH_CHECK_TIMEOUT_DEFAULT = 1000;

    private final DynamicIntProperty HEALTH_CHECK_TIMEOUT_MILLIS =
            DynamicPropertyFactory.getInstance().getIntProperty(PropertyNames.HEALTH_CHECK_TIMEOUT_MILLIS,
                    HEALTH_CHECK_TIMEOUT_DEFAULT);

    private HealthCheckHandler healthCheckHandler;
    private AtomicReference<HealthCheckTask> currentFuture;
    private SynchronousQueue<Boolean> healthCheckFeeder;
    private Thread healthChecker;

    @Inject
    public AsyncHealthCheckInvocationStrategy(HealthCheckHandler healthCheckHandler) {
        logger.info(String.format("Application health check handler to be used by karyon: %s", healthCheckHandler.getClass().getName()));
        this.healthCheckHandler = healthCheckHandler;
        currentFuture = new AtomicReference<HealthCheckTask>(new HealthCheckTask());
        healthCheckFeeder = new SynchronousQueue<Boolean>();
        healthChecker = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (healthCheckFeeder.take()) { // blocks till something is available.
                        currentFuture.get().run();
                    }
                    logger.info("Karyon health check thread swallowed the poison pill and stopped gracefully!");
                } catch (InterruptedException e) {
                    logger.info("Karyon health check thread interrupted, no new health checks will be done and the health will always be unhealthy.");
                    currentFuture.set(new HealthCheckTask(true));
                }
            }
        });
        healthChecker.setDaemon(true);
        healthChecker.start();
        healthCheckFeeder.offer(true); // Bootstrap one check so our current future always has a value.
    }

    @Override
    public int invokeCheck() throws TimeoutException {
        healthCheckFeeder.offer(true);

        Future<Integer> currFuture = currentFuture.get();
        try {
            return currFuture.get(HEALTH_CHECK_TIMEOUT_MILLIS.get(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.interrupted(); /// reset the interrupted status.
            logger.error("Async health check interrupted, this is deemed as failure.", e);
        } catch (TimeoutException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Async health check failed, this is deemed as failure.", e);
        }
        return 500;
    }

    public void stop() throws InterruptedException {
        healthCheckFeeder.put(false);
    }

    private class HealthCheckTask extends FutureTask<Integer> {

        private HealthCheckTask() {
            this(false);
        }

        private HealthCheckTask(final boolean alwaysUnhealthy) {
            super(new Callable<Integer>() {
                @Override
                public Integer call() throws Exception {
                    if (alwaysUnhealthy) {
                        return 500;
                    }
                    return healthCheckHandler.checkHealth();
                }
            });
        }

        @Override
        protected void done() {
            currentFuture.compareAndSet(this, new HealthCheckTask()); // Whenever it is done, do a fresh check on demand
        }
    }
}
