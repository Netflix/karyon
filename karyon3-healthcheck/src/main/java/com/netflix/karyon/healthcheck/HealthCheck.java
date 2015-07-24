package com.netflix.karyon.healthcheck;

public interface HealthCheck {
    /**
     * Perform the health check synchronously
     * @return
     */
    HealthStatus check();
}
