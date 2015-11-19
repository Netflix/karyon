package com.netflix.karyon.api.health;

import java.util.List;

/**
 * Instance of a HealthCheck response and utility methods for creating specific statuses
 */
public interface HealthCheckStatus {
    /**
     * @return Final health check status
     */
    HealthState getState();

    /**
     * @return List of indicator statuses that were used to determine HealthCheck
     */
    List<HealthIndicatorStatus> getIndicators();
    
    public static HealthCheckStatus create(HealthState state, List<HealthIndicatorStatus> indicators) {
        return new HealthCheckStatus() {
            @Override
            public HealthState getState() {
                return state;
            }

            @Override
            public List<HealthIndicatorStatus> getIndicators() {
                return indicators;
            }
        };
    }
}
