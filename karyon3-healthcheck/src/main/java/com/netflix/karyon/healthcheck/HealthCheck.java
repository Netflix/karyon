package com.netflix.karyon.healthcheck;

/**
 * Basic interface for defining a health check.  Karyon injects a single top level HealthCheck binding
 * to invoke the health check.  A HealthCheck implementation may implement the health check logic manually
 * or use multi-bindings to combine multiple HealthChecks (@see CompositeHealthCheck).
 * 
 * To define a health check,
 * 
 * <pre>
 * {@code 
 * bind(HealthCheck.class).to(MyHealthCheck.class);
 * 
 * public class MyHealthCheck implements HealthCheck {
 *     public HealthStatus check() {
 *         return HealthStatuses.healthy();
 *     }
 * }
 * }
 * </pre>
 * 
 * @author elandau
 *
 */
public interface HealthCheck {
    /**
     * Perform the health check synchronously
     * @return
     */
    HealthStatus check();
}
