package com.netflix.karyon.healthcheck;

import java.util.concurrent.CompletableFuture;

/**
 * Basic interface for defining health indication logic.  0 or more HealthIndicators are used to determine 
 * the application {@link HealthCheck}.  HealthIndicators are tracked by a {@link HealthIndicatorRegistry}
 * where the default implementation uses all HealthIndicators registered as a set multibinding.  
 * 
 * HealthIndicator can inject types that are to be consulted for health indication, call out to shell 
 * scripts or call a remote service.  
 * 
 * To register a head indicator,
 * <pre>
 * {@code
 * Multbindings.newSetBinder(binder()).addBinding().to(MyHealthIndicator.class);
 * }
 * </pre>
 *  
 * @author elandau
 *
 */
public interface HealthIndicator {
    /**
     * Perform the health check asynchronously.
     */
    CompletableFuture<HealthIndicatorStatus> check();
    
    /**
     * Return the name of the health indicator.  Note that health indicators with duplicate names are allowed.
     */
    String getName();
}
