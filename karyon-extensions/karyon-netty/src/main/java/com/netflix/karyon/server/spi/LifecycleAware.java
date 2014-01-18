package com.netflix.karyon.server.spi;

/**
 * Lifecycle methods for a request router.
 *
 * @author Nitesh Kant
 */
public interface LifecycleAware {

    /**
     * Starts a lifecycle aware router.
     */
    void start();

    /**
     * Stops a lifecycle aware router.
     */
    void stop();
}
