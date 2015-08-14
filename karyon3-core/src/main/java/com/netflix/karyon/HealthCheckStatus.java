package com.netflix.karyon;

import java.util.Collections;
import java.util.List;

import com.netflix.karyon.healthcheck.HealthIndicatorStatus;

/**
 * Immutable status returned by {@link HealthCheck}
 * 
 * @author elandau
 *
 */
public class HealthCheckStatus {
    private final LifecycleState state;
    private final List<HealthIndicatorStatus> indicators;
    
    public static HealthCheckStatus healthy(LifecycleState state) {
        return new HealthCheckStatus(state, Collections.emptyList());
    }
    
    public HealthCheckStatus(LifecycleState state, List<HealthIndicatorStatus> indicators) {
        this.state = state;
        this.indicators = indicators;
    }
    
    public LifecycleState getState() {
        return state;
    }
    
    public List<HealthIndicatorStatus> getIndicators() {
        return indicators;
    }
}
