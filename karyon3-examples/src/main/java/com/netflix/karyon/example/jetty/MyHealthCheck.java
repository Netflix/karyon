package com.netflix.karyon.example.jetty;

import com.netflix.karyon.healthcheck.HealthCheck;
import com.netflix.karyon.healthcheck.HealthStatus;
import com.netflix.karyon.healthcheck.HealthStatuses;

public class MyHealthCheck implements HealthCheck {
    @Override
    public HealthStatus check() {
        return HealthStatuses.healthy();
    }
}
