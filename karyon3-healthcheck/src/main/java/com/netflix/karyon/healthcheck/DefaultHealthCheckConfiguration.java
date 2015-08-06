package com.netflix.karyon.healthcheck;

public class DefaultHealthCheckConfiguration implements HealthCheckConfiguration {
    @Override
    public int getCacheInterval() {
        return 30;
    }
}
