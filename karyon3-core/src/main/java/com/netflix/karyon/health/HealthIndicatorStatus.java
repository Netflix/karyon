package com.netflix.karyon.health;

import java.util.Map;

/**
 * Immutable health check instance returned from a {@link HealthIndicator}
 * 
 * See {@link HealthIndicatorStatus} for utility methods to create HealthIndicatorStatus objects
 * @author elandau
 */
public interface HealthIndicatorStatus {
    /**
     * @return Map of named attributes that provide additional information regarding the health.
     * For example, a CPU health check may return Unhealthy with attribute "usage"="90%"
     */
    public Map<String, Object> getAttributes();
    
    /**
     * @return True if healthy or false otherwise.
     */
    public boolean isHealthy();
    
    /**
     * @return Exception providing additional information regarding the failure state.  This could be
     * the last known exception. 
     */
    public String getError();
    
    /**
     * @return True if the status includes a error description
     */
    public boolean hasError();
    
    /**
     * @return Time when healthcheck was conducted
     */
    public String getTimestamp();
    
    /**
     * @return Name of HealthIndicator from which this status was created
     */
    public String getName();
}
