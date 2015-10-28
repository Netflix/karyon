package com.netflix.karyon.api.health;

/**
 * Health state of an application
 */
public enum HealthState {
    Starting,
    Healthy,
    Unhealthy,
    OutOfService
}
