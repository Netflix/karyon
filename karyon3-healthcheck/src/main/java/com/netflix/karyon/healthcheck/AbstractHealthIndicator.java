package com.netflix.karyon.healthcheck;

public abstract class AbstractHealthIndicator implements HealthIndicator {
    private final String name;

    public AbstractHealthIndicator(String name) {
        this.name = name;
    }
    
    @Override
    public String getName() {
        return name;
    }
}
