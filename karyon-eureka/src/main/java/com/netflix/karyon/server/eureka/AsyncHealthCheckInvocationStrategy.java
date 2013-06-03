/*
 * Copyright 2013 Netflix, Inc.
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */

package com.netflix.karyon.server.eureka;

import com.google.inject.Inject;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.governator.annotations.Configuration;
import com.netflix.governator.guice.lazy.LazySingleton;
import com.netflix.karyon.spi.HealthCheckHandler;
import com.netflix.karyon.spi.PropertyNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * An implementation of {@link HealthCheckInvocationStrategy} that calls the underlying {@link HealthCheckHandler}
 * asynchronously. If the underlying handler takes more time than configured in the property
 * {@link com.netflix.karyon.spi.PropertyNames#HEALTH_CHECK_TIMEOUT_MILLIS}, this handler will throw a {@link TimeoutException}.
 * The timeout is retrieved using archaius's dynamic properties and hence can be changed at runtime. The default timeout
 * value is as configured by the property{@link PropertyNames#HEALTH_CHECK_TIMEOUT_DEFAULT_MILLIS} milliseconds. <p/>
 *
 * <h3>Threading model</h3>
 * This class uses exactly one thread to perform the health check. There is no queue used for any concurrent health
 * check requests. Any concurrent health check requests blocks (for the configured timeout) and wait for the single
 * execution to finish after which the same result is returned. This is useful in providing an SLA for the healthcheck
 * and does not create a queuing point which may result in unnecessary tuning and memory overheads.
 * 
 * @author Nitesh Kant
 */
@LazySingleton
public class AsyncHealthCheckInvocationStrategy implements HealthCheckInvocationStrategy {

    protected static final Logger logger = LoggerFactory.getLogger(AsyncHealthCheckInvocationStrategy.class);

    @Configuration(
            value = PropertyNames.HEALTH_CHECK_TIMEOUT_DEFAULT_MILLIS,
            documentation = "Default timeout value for healthchecks in milliseconds."
    )
    protected int HEALTH_CHECK_TIMEOUT_DEFAULT = 1000;

    private final DynamicIntProperty HEALTH_CHECK_TIMEOUT_MILLIS =
            DynamicPropertyFactory.getInstance().getIntProperty(PropertyNames.HEALTH_CHECK_TIMEOUT_MILLIS,
                    HEALTH_CHECK_TIMEOUT_DEFAULT);

    private HealthCheckHandler healthCheckHandler;
    private AtomicReference<HealthCheckTask> currentFuture;
    private SynchronousQueue<Boolean> healthCheckFeeder;

    /**
     * This is used when {@link #invokeCheck()} gets a future which is not yet started the execution. This essentially
     * will be a race-condition where-in the task finishes execution between the time {@link #invokeCheck()} signals
     * the health check thread to execute the handler and does a {@link #currentFuture#get()}. In this case, we just
     * use the last computed value.
     */
    private volatile int lastComputedStatus = 500;

    private Thread healthChecker;
    private AtomicBoolean started = new AtomicBoolean();

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
    }

    @PostConstruct
    public void start() {
        if (!started.compareAndSet(false, true)) {
            return;
        }
        healthChecker.start();
    }

    @Override
    public int invokeCheck() throws TimeoutException {
        if (!healthCheckFeeder.offer(true)) {
            logger.debug("Async healthcheck already in progress, will use the existing result.");
        }

        HealthCheckTask currFuture = currentFuture.get();
        try {
            return currFuture.get(HEALTH_CHECK_TIMEOUT_MILLIS.get(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.interrupted(); /// reset the interrupted status.
            logger.error("Async health check interrupted, this is deemed as failure.", e);
        } catch (TimeoutException e) {
            if (!currFuture.isStarted()) { // This will happen if the health check task finishes between healthCheckFeeder.offer() & currentFuture.get()
                if (logger.isDebugEnabled()) {
                    logger.debug("Async health check strategy got a future that was not started, returning last computed status: " + lastComputedStatus);
                }
                return lastComputedStatus;
            }
            throw e;
        } catch (Exception e) {
            logger.error("Async health check failed, this is deemed as failure.", e);
        }
        return 500;
    }

    @Override
    public HealthCheckHandler getHandler() {
        return healthCheckHandler;
    }

    public void stop() throws InterruptedException {
        if (healthCheckFeeder.offer(false)) {
            logger.info("Healthchecker poison pill offer failed, interrupting the thread.");
            healthChecker.interrupt();
        }
    }

    private class HealthCheckTask extends FutureTask<Integer> {

        private volatile boolean started;

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
                    return healthCheckHandler.getStatus();
                }
            });
        }

        @Override
        public void run() {
            started = true;
            super.run();
        }

        public boolean isStarted() {
            return started;
        }

        @Override
        protected void done() {
            if (!isCancelled()) {
                try {
                    lastComputedStatus = get(1, TimeUnit.MILLISECONDS);
                } catch (Exception e) {
                    logger.info(
                            "Failed to set the last computed status for health check invocation. Current last computed status: "
                            + lastComputedStatus, e);
                }
            }
            currentFuture.compareAndSet(this, new HealthCheckTask()); // Whenever it is done, do a fresh check on demand
        }
    }
}
