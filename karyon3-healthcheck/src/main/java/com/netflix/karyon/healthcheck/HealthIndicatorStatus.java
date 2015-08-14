package com.netflix.karyon.healthcheck;

import java.util.Map;

/**
 * Immutable health check instance returned from a {@link HealthIndicator}
 * 
 * See {@link HealthIndicatorStatus} for utility methods to create HealthIndicatorStatus objects
 * @author elandau
 */
public interface HealthIndicatorStatus {
    /**
     * Map of named attributes that provide additional information regarding the health.
     * For example, a CPU health check may return Unhealthy with attribute "usage"="90%"
     * 
     * @return
     */
    public Map<String, Object> getAttributes();
    
    /**
     * Return the state information 
     * @return
     */
    public boolean isHealthy();
    
    /**
     * Exception providing additional information regarding the failure state.  This could be
     * the last known exception. 
     */
    public Throwable getError();
    
    /**
     * The status include an exception
     * @return
     */
    public boolean hasError();
    
    /**
     * Time when healthcheck was conducted
     * @return
     */
    public String getTimestamp();
    
    /**
     * Return name of HealthIndicator from which this status was created
     * @return
     */
    public String getName();
}
