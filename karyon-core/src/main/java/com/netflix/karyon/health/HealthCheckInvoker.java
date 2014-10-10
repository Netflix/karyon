package com.netflix.karyon.health;

import java.util.Collection;
import java.util.List;

import com.google.inject.ImplementedBy;

/**
 * Strategy for invoking the actual healthcheck operations
 * 
 * @author elandau
 *
 */
@ImplementedBy(InlineHealthCheckInvoker.class)
public interface HealthCheckInvoker {
    public List<HealthCheck.Status> invoke(Collection<HealthCheck> healthChecks);
}
