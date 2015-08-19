package com.netflix.karyon.health;

public abstract class AbstractHealthIndicator implements HealthIndicator {
    private final String name;

    public AbstractHealthIndicator(String name) {
        this.name = name;
    }
    
    public AbstractHealthIndicator() {
        this.name = getClass().getSimpleName();
    }
    
    @Override
    public String getName() {
        return name;
    }
}
