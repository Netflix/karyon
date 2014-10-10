package com.netflix.karyon.health;

/**
 * Implementation of HealthCheck that may be set manually.
 * 
 * @author elandau
 */
public class ManualHealthCheck implements HealthCheck {
    private volatile Status status;
    private final String name;
    
    public ManualHealthCheck() {
        this("manual");
    }

    public ManualHealthCheck(String name) {
        this.name = name;
        this.status = Status.error(this, null);
    }

    public void unhealthy(Throwable error) {
        this.status = Status.error(this, error);
    }
    
    public void healthy() {
        this.status = Status.ready(this);
    }
    
    @Override
    public String getName() {
        return name;
    }

    @Override
    public Status check() {
        return status;
    }

    @Override
    public String toString() {
        return "ManualHealthCheck [status=" + status + ", name=" + name + "]";
    }
}
