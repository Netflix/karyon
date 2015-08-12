package com.netflix.karyon.healthcheck;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class HealthCheckTest {
    
    public static HealthCheck delayed(long amount, TimeUnit units, HealthStatus status) {
        return new HealthCheck() {
            @Override
            public CompletableFuture<HealthStatus> check() {
                final CompletableFuture<HealthStatus> future = new CompletableFuture<HealthStatus>();
                Executors.newSingleThreadScheduledExecutor().schedule(() -> { 
                    System.out.println("Timeout");
                    future.complete(status); 
                }, amount, units);
                return future;
            }
        };
    }

    @Test
    public void testAsyncStatus() throws InterruptedException, ExecutionException {
        HealthCheck check = delayed(1, TimeUnit.SECONDS, HealthStatuses.healthy());
        TimeUnit.SECONDS.sleep(2);
        check.check().whenComplete((result, error) -> System.out.println("Completed"));
        TimeUnit.SECONDS.sleep(2);
    }
    
    
    @Test
    public void testNoHealthStatuses() {
        Injector injector = Guice.createInjector();
        CompositeHealthCheck manager = injector.getInstance(CompositeHealthCheck.class);
        
        HealthStatus status = manager.check().join();
        Assert.assertEquals(true, status.isHealthy());
    }
    
    @Test
    public void testOneHealthStatus() {
        Map<String, HealthCheck> healthChecks = new HashMap<String, HealthCheck>();
        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(HealthCheckRegistry.class).toInstance(new HealthCheckRegistry() {
                    @Override
                    public Map<String, HealthCheck> getHealthChecks() {
                        return healthChecks;
                    }
                });
            }
        });
        
        CompositeHealthCheck manager = injector.getInstance(CompositeHealthCheck.class);
        
        HealthStatus status = manager.check().join();
        Assert.assertEquals(true, status.isHealthy());
        
        healthChecks.put("foo", HealthChecks.alwaysHealthy());
        status = manager.check().join();
        Assert.assertEquals(true, status.isHealthy());
        
        healthChecks.put("foo", HealthChecks.alwaysUnhealthy());
        status = manager.check().join();
        Assert.assertEquals(false, status.isHealthy());
    }
    
    @Test
    public void testMultipleHealthStatuses() {
        Map<String, HealthCheck> healthChecks = new HashMap<String, HealthCheck>();
        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(HealthCheckRegistry.class).toInstance(new HealthCheckRegistry() {
                    @Override
                    public Map<String, HealthCheck> getHealthChecks() {
                        return healthChecks;
                    }
                });
            }
        });
        
        CompositeHealthCheck manager = injector.getInstance(CompositeHealthCheck.class);
        HealthCheckRegistry registry = injector.getInstance(HealthCheckRegistry.class);
        
        HealthStatus status = manager.check().join();
        Assert.assertEquals(true, status.isHealthy());
        Assert.assertEquals(0, status.getAttributes().size());
        
        healthChecks.put("foo", HealthChecks.alwaysHealthy());
        healthChecks.put("bar", HealthChecks.alwaysHealthy());
        status = manager.check().join();
        Assert.assertEquals(true, status.isHealthy());
        Assert.assertEquals(2, status.getAttributes().size());
        
        healthChecks.put("foo", HealthChecks.alwaysUnhealthy());
        status = manager.check().join();
        Assert.assertEquals(false, status.isHealthy());
        Assert.assertEquals(2, status.getAttributes().size());
        
        // This will never happen in reality but interesting to test
        healthChecks.remove("foo");
        status = manager.check().join();
        Assert.assertEquals(true, status.isHealthy());
        Assert.assertEquals(1, status.getAttributes().size());
    }
}
