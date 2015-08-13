package com.netflix.karyon.healthcheck;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Utility class for creating common HealthStatus instances
 * 
 * @author elandau
 *
 */
public final class HealthStatuses {
    
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss z");
    
    public static HealthStatus healthy() {
        return create(true, Collections.<String, Object>emptyMap(), null);
    }
    
    public static HealthStatus healthy(Map<String, Object> attr) {
        return create(true, attr, null);
    }
    
    public static HealthStatus unhealthy() {
        return create(false, Collections.<String, Object>emptyMap(), null);
    }
    
    public static HealthStatus unhealthy(Map<String, Object> attr) {
        return create(false, attr, null);
    }
    
    public static HealthStatus unhealthy(Throwable t) {
        return create(false, Collections.<String, Object>emptyMap(), t);
    }
    
    public static HealthStatus unhealthy(Map<String, Object> attr, Throwable t) {
        return create(false, attr, t);
    }
    
    public static HealthStatus create(final boolean isHealthy, final Map<String, Object> attributes, final Throwable error) {
        ZonedDateTime zonedDateTime = ZonedDateTime.now();
        return new HealthStatus() {
            @Override
            public Map<String, Object> getAttributes() {
                return Collections.unmodifiableMap(attributes);
            }

            @Override
            public boolean isHealthy() {
                return isHealthy;
            }

            @Override
            public Throwable getError() {
                return error;
            }

            @Override
            public boolean hasError() {
                return error != null;
            }

            @Override
            public String getTimestamp() {
                return DATE_TIME_FORMAT.format(zonedDateTime);
            }
            
            @Override
            public String toString() {
                StringBuilder sb = new StringBuilder();
                sb.append("HealthStatus[healthy=").append(isHealthy());
                if (attributes != null && !attributes.isEmpty()) {
                    sb.append(", attr=[");
                    for (Entry<String, Object> attr : attributes.entrySet()) {
                        sb.append(attr.getKey()).append(":").append(attr.getValue()).append(" ");
                    }
                    sb.append("]");
                }
                sb.append("]");
                return sb.toString();
            }
        };
    }
}
