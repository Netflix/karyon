package com.netflix.karyon;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.netflix.karyon.healthcheck.HealthIndicatorStatus;

/**
 * Immutable status returned by {@link HealthCheck}
 * 
 * @author elandau
 *
 */
public class HealthCheckStatus {
    private final LifecycleState state;
    private final List<HealthIndicatorStatus> statuses;
    private final Map<String, Object> attributes;
    
    public static HealthCheckStatus healthy(LifecycleState state) {
        return new HealthCheckStatus(state, Collections.emptyList(), Collections.emptyMap());
    }
    
    public HealthCheckStatus(LifecycleState state, List<HealthIndicatorStatus> statuses) {
        this(state, statuses, Collections.emptyMap());
    }
    
    public HealthCheckStatus(LifecycleState state, List<HealthIndicatorStatus> statuses, Map<String, Object> attributes) {
        this.state = state;
        this.statuses = statuses;
        this.attributes = attributes;
    }
    
    public Map<String, Object> getAttributes() {
        return attributes;
    }
    
    public LifecycleState getState() {
        return state;
    }
    
    public List<HealthIndicatorStatus> getStatuses() {
        return statuses;
    }
}
