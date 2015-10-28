package com.netflix.karyon.api.health;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

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
    
    static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss z");
    
    public static HealthIndicatorStatus healthy(String name) {
        return create(name, true, Collections.<String, Object>emptyMap(), null);
    }
    
    public static HealthIndicatorStatus healthy(String name, Map<String, Object> attr) {
        return create(name, true, attr, null);
    }
    
    public static HealthIndicatorStatus unhealthy(String name) {
        return create(name, false, Collections.<String, Object>emptyMap(), null);
    }
    
    public static HealthIndicatorStatus unhealthy(String name, Map<String, Object> attr) {
        return create(name, false, attr, null);
    }
    
    public static HealthIndicatorStatus unhealthy(String name, Throwable t) {
        return create(name, false, Collections.<String, Object>emptyMap(), t);
    }
    
    public static HealthIndicatorStatus unhealthy(String name, Map<String, Object> attr, Throwable t) {
        return create(name, false, attr, t);
    }
    
    public static HealthIndicatorStatus create(final String name, final boolean isHealthy, final Map<String, Object> attributes, final Throwable error) {
        ZonedDateTime zonedDateTime = ZonedDateTime.now();
        return new HealthIndicatorStatus() {
            @Override
            public Map<String, Object> getAttributes() {
                return Collections.unmodifiableMap(attributes);
            }

            @Override
            public boolean isHealthy() {
                return isHealthy;
            }

            @Override
            public String getError() {
                return error != null
                        ? error.toString()
                        : null;
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

            @Override
            public String getName() {
                return name;
            }
        };
    }
}
