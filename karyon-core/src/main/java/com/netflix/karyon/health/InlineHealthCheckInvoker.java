package com.netflix.karyon.health;

import java.util.Collection;
import java.util.List;

import javax.inject.Singleton;

import com.google.common.collect.Lists;
import com.netflix.karyon.health.HealthCheck.Status;

/**
 * Invoke all health checks in the context of the calling thread
 * @author elandau
 *
 */
@Singleton
public class InlineHealthCheckInvoker implements HealthCheckInvoker {
    @Override
    public List<Status> invoke(Collection<HealthCheck> healthChecks) {
        List<Status> response = Lists.newArrayList();
        for (HealthCheck hc : healthChecks) {
            response.add(hc.check());
        }
        return response;
    }
}
