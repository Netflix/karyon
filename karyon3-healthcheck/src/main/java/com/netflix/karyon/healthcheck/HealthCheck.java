package com.netflix.karyon.healthcheck;

import java.util.concurrent.CompletableFuture;

import com.google.inject.ImplementedBy;

/**
 * Basic interface for defining a health check.  Karyon injects a single top level HealthCheck binding
 * to invoke the health check.  A HealthCheck implementation may implement health check logic in a single
 * class or use multi-bindings to combine multiple HealthChecks (@see CompositeHealthCheck).
 * 
 * To define a single main health check,
 * 
 * <pre>
 * {@code
 * @Singleton
 * public class MyHealthCheck implements HealthCheck {
 *     public CompletableFuture<HealthStatus> check() {
 *         return CompletableFuture.complete(HealthStatuses.healthy());
 *     }
 * }
 * }
 * </pre>
 * 
 * A real health check can inject classes that are to be consulted for health status, call out to shell
 * scripts or call a remote service.  While the above example immediately returns a healthy status a
 * real implementation should be asynchronous. 
 * 
 * To bind a single health check just bind to the unqualified HealthCheck.class as shown below.  
 * 
 * <pre>
 * {@code 
 * bind(HealthCheck.class).to(MyHealthCheck.class);
 * }
 * </pre>
 * 
 * To combine multiple health checks see {@link CompositeHealthCheck}
 * 
 * See classes {@link HealthChecks} and {@link HealthStatuses} for utility methods to simplify creating
 * simple health statuses.
 * 
 * 
 * @author elandau
 *
 */
@ImplementedBy(AlwaysHealthyHealthCheck.class)
public interface HealthCheck {
    /**
     * Perform the health check asynchronously using a CompletableFuture.
     * @return
     */
    CompletableFuture<HealthStatus> check();
}
