package com.netflix.karyon.healthcheck;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class DefaultHealthCheckAggregator implements HealthCheckAggregator {

    @Override
    public HealthStatus aggregate(Map<String, HealthStatus> statuses) {
        Map<HealthState, Long> counts = new HashMap<>();
        
        Map<String, Object> attr = new HashMap<>();
        for (Entry<String, HealthStatus> entry : statuses.entrySet()) {
            HealthState state = entry.getValue().getState();
            
            attr.put(entry.getKey(), entry.getValue());
            
            Long count = counts.get(state);
            if (count == null) {
                count = 0L;
                counts.put(state, count);
            }
            count++;
            counts.put(state, count);
        }

        if (counts.containsKey(HealthState.States.STOPPED)) {
            return HealthStatuses.stopped(attr);
        }
        else if (counts.containsKey(HealthState.States.UNHEALTHY)) {
            // TODO: More meaningful health check exception
            return HealthStatuses.unhealthy(attr, new Exception("Some health checks unhealhty"));
        }
        else if (counts.containsKey(HealthState.States.STARTING)) {
            return HealthStatuses.starting(attr);
        }
        else if (counts.containsKey(HealthState.States.DEGRADED)) {
            return HealthStatuses.degraded(attr);
        }
        else {
            return HealthStatuses.healthy(attr);
        }
    }
}
