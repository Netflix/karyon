package com.netflix.karyon.spi;

import com.google.common.annotations.Beta;

/**
 * Contract for a service registry to be used by karyon.<br/>
 * Karyon comes with eureka as the service registry but can be overridden by any custom service registry.
 *
 * @author Nitesh Kant
 */
public interface ServiceRegistryClient {

    /**
     * The status of a service. As of now, karyon supports these statuses but the underlying implementation can support
     * much varied statuses and can be called directly by the application in whichever place it deems necessary.
     */
    @Beta
    enum ServiceStatus { UP, DOWN }

    /**
     * Updates the status of the service to the passed <code>newStatus</code>.
     *
     * @param newStatus New status for the application.
     */
    void updateStatus(ServiceStatus newStatus);
}
