package com.netflix.karyon.healthcheck;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Default implementation for aggregating multiple resolved health statueses into a single
 * HealthStatus where the composite HealthStatus is healthy only if ALL child HealthStatus's
 * are healthy.
 * 
 * @author elandau
 *
 */
public class DefaultHealthCheckAggregator implements HealthCheckAggregator {

    @Override
    public HealthStatus aggregate(Map<String, HealthStatus> statuses) {
        boolean isHealthy = true;
        
        Map<String, Object> attr = new HashMap<>();
        for (Entry<String, HealthStatus> entry : statuses.entrySet()) {
            if (!entry.getValue().isHealthy()) {
                isHealthy = false;
            }
            
            attr.put(entry.getKey(), entry.getValue());
        }

        return HealthStatuses.create(isHealthy, attr, null);
    }
}
