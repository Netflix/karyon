package com.netflix.karyon.eureka;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.inject.Provider;
import javax.inject.Singleton;

import com.google.inject.Inject;
import com.google.inject.util.Providers;
import com.netflix.karyon.health.HealthCheckHandler;
import com.netflix.karyon.health.HealthCheck;
import com.netflix.karyon.health.HealthCheckRegistry;

/**
 * Simple health check registry that supports the following HealthChecks
 * 1.  Bridge for ApplicationInfoManager's manual HealthCheck status
 * 2.  Bridge for HealthCheckHandler
 * 3.  Set<HealthCheck> creating using guice's multibinding
 * 
 * Note that all healthchecks are injected lazily using Providers to ensure there is no
 * circular dependency for components that depend on DiscoveryClient.
 * 
 * @author elandau
 */
@Singleton
public class DefaultHealthCheckRegistry implements HealthCheckRegistry {
	private final CopyOnWriteArrayList<Provider<? extends HealthCheck>> healthChecks = new CopyOnWriteArrayList<Provider<? extends HealthCheck>>();
	
	public static class OptionalArgs {
	    @Inject(optional=true)
	    Provider<HealthCheckHandler> handler;    
	    
        @Inject(optional=true)
	    Set<Provider<HealthCheck>> healthChecks;
	}
	
	@Inject
    DefaultHealthCheckRegistry(Provider<ApplicationInfoManagerHealthCheck> manager, OptionalArgs args) {
	    this(args.healthChecks, manager, args.handler);
	}
	
    public DefaultHealthCheckRegistry(
            final Set<Provider<HealthCheck>> healthChecks, 
            final Provider<ApplicationInfoManagerHealthCheck> manager, 
            final Provider<HealthCheckHandler> handler) {
        
        if (manager != null) {
            this.healthChecks.add(manager);
        }
        
	    if (handler != null) {
    	    this.healthChecks.add(Providers.of(new HealthCheckHandlerToHealthCheckAdapter(handler, "legacy")));
	    }
	    
	    if (healthChecks != null) {
	        this.healthChecks.addAll(healthChecks);
	    }
	}

    /**
	 * Return a list of ALL registered handlers
	 */
	@Override
	public List<HealthCheck> getHealthChecks() {
        List<HealthCheck> statuses = new ArrayList<HealthCheck>();
        for (Provider<? extends HealthCheck> provider : healthChecks) {
            HealthCheck hc = provider.get();
            if (hc != null) {
                statuses.add(provider.get());
            }
        }
        return statuses;
    }

    @Override
    public void registerHealthCheck(HealthCheck handler) {
        this.healthChecks.add(Providers.of(handler));
    }
}
