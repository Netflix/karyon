package com.netflix.karyon.healthcheck;

import java.util.Map;

/**
 * Immutable health check instance returned from a single call to 
 * HealthCheck.check()
 * 
 * @author elandau
 *
 */
public interface HealthStatus {
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
    public HealthState getState();
    
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
}
