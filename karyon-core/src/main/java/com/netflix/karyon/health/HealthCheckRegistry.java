package com.netflix.karyon.health;

import java.util.List;

/**
 * Registry of all status checkers.  The actual health check is performed
 * by an implementation of {@link HealthCheckInvoker}
 * 
 * @author elandau
 */
public interface HealthCheckRegistry {
    /**
     * @return Return list of all registered HealthCheck conditions
     */
	List<HealthCheck> getHealthChecks();

	/**
	 * Add a HealthCheck to the registry
	 *  
     * @param handler
	 */
    void registerHealthCheck(HealthCheck handler);
}
