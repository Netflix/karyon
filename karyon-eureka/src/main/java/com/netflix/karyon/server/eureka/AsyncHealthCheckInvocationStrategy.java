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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.SettableFuture;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.governator.annotations.Configuration;
import com.netflix.governator.guice.lazy.LazySingleton;
import com.netflix.karyon.spi.HealthCheckHandler;
import com.netflix.karyon.spi.PropertyNames;

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

    private final DynamicIntProperty HEALTH_CHECK_TIMEOUT_MILLIS;
    private final HealthCheckHandler healthCheckHandler;
    
    private AtomicReference<Future<Integer>> currentFuture = new AtomicReference<Future<Integer>>();
    private ExecutorService executor;
    private AtomicInteger executeCounter = new AtomicInteger(0);
    private AtomicInteger invokeCounter = new AtomicInteger(0);
    
    @Inject
    public AsyncHealthCheckInvocationStrategy(HealthCheckHandler healthCheckHandler) {
        logger.info(String.format("Application health check handler to be used by karyon: %s", healthCheckHandler.getClass().getName()));
        this.healthCheckHandler = healthCheckHandler;
        this.currentFuture = new AtomicReference<Future<Integer>>();
        this.HEALTH_CHECK_TIMEOUT_MILLIS = DynamicPropertyFactory.getInstance().getIntProperty(PropertyNames.HEALTH_CHECK_TIMEOUT_MILLIS,
                HEALTH_CHECK_TIMEOUT_DEFAULT);
    }

    @VisibleForTesting
    public AsyncHealthCheckInvocationStrategy(HealthCheckHandler healthCheckHandler, int timeout) {
        logger.info(String.format("Application health check handler to be used by karyon: %s", healthCheckHandler.getClass().getName()));
        this.healthCheckHandler = healthCheckHandler;
        this.currentFuture = new AtomicReference<Future<Integer>>();
        this.HEALTH_CHECK_TIMEOUT_MILLIS = DynamicPropertyFactory.getInstance().getIntProperty(PropertyNames.HEALTH_CHECK_TIMEOUT_MILLIS,
                timeout);
    }
    
    @PostConstruct
    public synchronized void start() {
        if (this.executor == null) {
            this.executor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("AsyncHealthCheck-%d").setDaemon(true).build());
        }
    }

    @Override
    public int invokeCheck() throws TimeoutException {
        invokeCounter.incrementAndGet();
        
        while (true) {
            final SettableFuture<Integer> future = SettableFuture.create();
            // Use CAS on a settable future to take ownership of calculating the health check
            if (currentFuture.compareAndSet(null, future)) {
                executor.submit(new Runnable() {
                    @Override
                    public void run() {
                        executeCounter.incrementAndGet();
                        try {
                            future.set(healthCheckHandler.getStatus());
                        }
                        catch (Exception e) {
                            logger.debug("Async health check had an error {}", e.getMessage());
                            // Any error translated to a 500
                            future.set(500);
                        }
                        finally {
                            // Only allow resetting of the state from within the worker 
                            // thread
                            currentFuture.set(null);
                        }
                    }
                });
                
                try {
                    return future.get(HEALTH_CHECK_TIMEOUT_MILLIS.get(), TimeUnit.MILLISECONDS);
                }
                catch (TimeoutException e) {
                    throw new TimeoutException();
                }
                catch (Exception e) {
                    future.set(500);
                }
            }
            // Health check is already being performed so try to attach to the existing test
            // or try again in case the pending check completed
            else {
                Future<Integer> pending =  currentFuture.get();
                if (pending != null) {
                    try {
                        return pending.get(HEALTH_CHECK_TIMEOUT_MILLIS.get(), TimeUnit.MILLISECONDS);
                    }
                    catch (TimeoutException e) {
                        throw new TimeoutException();
                    }
                    catch (Exception e) {
                        return 500;
                    }
                }
            }
        }
    }

    @Override
    public HealthCheckHandler getHandler() {
        return healthCheckHandler;
    }

    int getInvokeCounter() {
        return this.invokeCounter.get();
    }
    
    int getExecuteCounter() {
        return this.executeCounter.get();
    }
    
    public synchronized void stop() throws InterruptedException {
        if (executor != null) {
            executor.shutdown();
            executor = null;
        }
    }
}
