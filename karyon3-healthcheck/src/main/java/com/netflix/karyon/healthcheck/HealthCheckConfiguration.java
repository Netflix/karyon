package com.netflix.karyon.healthcheck;

import com.google.inject.ImplementedBy;

@ImplementedBy(DefaultHealthCheckConfiguration.class)
public interface HealthCheckConfiguration {
    int getCacheInterval();
}
