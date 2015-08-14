package com.netflix.karyon.healthcheck;

import java.util.Collections;
import java.util.List;

import javax.inject.Singleton;

@Singleton
public class EmptyHealthIndicatorRegistry implements HealthIndicatorRegistry {
    @Override
    public List<HealthIndicator> getHealthIndicators() {
        return Collections.emptyList();
    }
}
