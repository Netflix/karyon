package com.netflix.karyon.spi.health;

import java.util.Collections;
import java.util.Map;

import com.netflix.karyon.api.health.HealthIndicator;
import com.netflix.karyon.api.health.HealthIndicatorStatus;

public abstract class AbstractHealthIndicator implements HealthIndicator {
    private final String name;

    /**
     *  Creates an AbstractHealthIndicator with the specified name
     * 
     * @param name name     Name of this Health Indicator
     * 
     */
    public AbstractHealthIndicator(String name) {
        this.name = name;
    }
    
    /** 
     * Creates an AbstractHealthIndicator with a default name
     * 
     */
    public AbstractHealthIndicator() {
        this.name = getClass().getSimpleName();
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    /**
     * 
     * Create a healthy status
     * 
     * @return a healthy status
     */

    protected final HealthIndicatorStatus healthy() {
        return HealthIndicatorStatus.create(getName(), true, Collections.<String, Object> emptyMap(), null);
    }

    /**
     * 
     * Create a healthy status
     *
     * @param attr  Map of the attributes describing status
     * @return a healthy status
     */
    protected final HealthIndicatorStatus healthy(Map<String, Object> attr) {
        return HealthIndicatorStatus.create(getName(), true, attr, null);
    }

    /**
     * 
     * Create an unhealthy status
     * 
     * @return a unhealthy status
     */
    protected final HealthIndicatorStatus unhealthy() {
        return HealthIndicatorStatus.create(getName(), false, Collections.<String, Object> emptyMap(), null);
    }

    /**
     * 
     * Create an unhealthy status
     * 
     * @return a unhealthy status
     */
    protected final HealthIndicatorStatus unhealthy(Map<String, Object> attr) {
        return HealthIndicatorStatus.create(getName(), false, attr, null);
    }

    /**
     * 
     * Create an unhealthy status
     * 
     * @return a unhealthy status
     */
    protected final HealthIndicatorStatus unhealthy(Throwable t) {
        return HealthIndicatorStatus.create(getName(), false, Collections.<String, Object> emptyMap(), t);
    }

    /**
     * 
     * Create an unhealthy status
     * 
     * @return a unhealthy status
     */
    protected final HealthIndicatorStatus unhealthy(Map<String, Object> attr, Throwable t) {
        return HealthIndicatorStatus.create(getName(), false, attr, t);
    }

}
