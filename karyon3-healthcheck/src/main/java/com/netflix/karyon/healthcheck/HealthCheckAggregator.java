package com.netflix.karyon.healthcheck;

import java.util.Map;

import com.google.inject.ImplementedBy;

/**
 * Policy for aggregating multiple HealthStatus's into a single HealthStatus that will
 * normally become the HealthStatus exposed by the application.  
 * 
 * @author elandau
 */
@ImplementedBy(DefaultHealthCheckAggregator.class)
public interface HealthCheckAggregator {
    HealthStatus aggregate(Map<String, HealthStatus> statuses);
}
