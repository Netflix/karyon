package com.netflix.karyon.healthcheck;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;

public class HealthCheckTest {
    public void testNoHealthStatuses() {
        Injector injector = Guice.createInjector(new HealthCheckModule());
        HealthCheckManager manager = injector.getInstance(HealthCheckManager.class);
        
        HealthStatus status = manager.check();
        Assert.assertEquals(HealthState.States.STARTING, status.getState());
    }
    
    @Test
    public void testOneHealthStatus() {
        Map<String, HealthCheck> healthChecks = new HashMap<String, HealthCheck>();
        Injector injector = Guice.createInjector(Modules.override(new HealthCheckModule()).with(new AbstractModule() {
            @Override
            protected void configure() {
                bind(HealthCheckRegistry.class).toInstance(new HealthCheckRegistry() {
                    @Override
                    public Map<String, HealthCheck> getHealthChecks() {
                        return healthChecks;
                    }
                });
            }
        }));
        
        HealthCheckManager manager = injector.getInstance(HealthCheckManager.class);
        
        HealthStatus status = manager.check();
        Assert.assertEquals(HealthState.States.HEALTHY, status.getState());
        
        healthChecks.put("foo", HealthChecks.alwaysHealthy());
        status = manager.check();
        Assert.assertEquals(HealthState.States.HEALTHY, status.getState());
        
        healthChecks.put("foo", HealthChecks.alwaysUnhealthy());
        status = manager.check();
        Assert.assertEquals(HealthState.States.UNHEALTHY, status.getState());
    }
    
    @Test
    public void testMultipleHealthStatuses() {
        Map<String, HealthCheck> healthChecks = new HashMap<String, HealthCheck>();
        Injector injector = Guice.createInjector(Modules.override(new HealthCheckModule()).with(new AbstractModule() {
            @Override
            protected void configure() {
                bind(HealthCheckRegistry.class).toInstance(new HealthCheckRegistry() {
                    @Override
                    public Map<String, HealthCheck> getHealthChecks() {
                        return healthChecks;
                    }
                });
            }
        }));
        
        HealthCheckManager manager = injector.getInstance(HealthCheckManager.class);
        HealthCheckRegistry registry = injector.getInstance(HealthCheckRegistry.class);
        
        HealthStatus status = manager.check();
        Assert.assertEquals(HealthState.States.HEALTHY, status.getState());
        Assert.assertEquals(0, status.getAttributes().size());
        
        healthChecks.put("foo", HealthChecks.alwaysHealthy());
        healthChecks.put("bar", HealthChecks.alwaysHealthy());
        status = manager.check();
        Assert.assertEquals(HealthState.States.HEALTHY, status.getState());
        Assert.assertEquals(2, status.getAttributes().size());
        
        healthChecks.put("foo", HealthChecks.alwaysUnhealthy());
        status = manager.check();
        Assert.assertEquals(HealthState.States.UNHEALTHY, status.getState());
        Assert.assertEquals(2, status.getAttributes().size());
        
        // This will never happen in reality but interesting to test
        healthChecks.remove("foo");
        status = manager.check();
        Assert.assertEquals(HealthState.States.HEALTHY, status.getState());
        Assert.assertEquals(1, status.getAttributes().size());
    }
}
