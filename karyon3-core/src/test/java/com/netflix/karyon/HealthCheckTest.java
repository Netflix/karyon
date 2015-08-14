package com.netflix.karyon;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.netflix.governator.Governator;
import com.netflix.karyon.healthcheck.HealthIndicator;
import com.netflix.karyon.healthcheck.HealthIndicatorRegistry;
import com.netflix.karyon.healthcheck.HealthIndicatorStatus;
import com.netflix.karyon.healthcheck.HealthIndicatorStatuses;
import com.netflix.karyon.healthcheck.HealthIndicators;

public class HealthCheckTest {
    
    public static HealthIndicator delayed(long amount, TimeUnit units, HealthIndicatorStatus status) {
        return new HealthIndicator() {
            @Override
            public CompletableFuture<HealthIndicatorStatus> check() {
                final CompletableFuture<HealthIndicatorStatus> future = new CompletableFuture<HealthIndicatorStatus>();
                Executors.newSingleThreadScheduledExecutor().schedule(() -> { 
                    System.out.println("Timeout");
                    future.complete(status); 
                }, amount, units);
                return future;
            }

            @Override
            public String getName() {
                return status.getName();
            }
        };
    }

    @Test
    public void testAsyncStatus() throws InterruptedException, ExecutionException {
        HealthIndicator check = delayed(1, TimeUnit.SECONDS, HealthIndicatorStatuses.healthy("foo"));
        TimeUnit.SECONDS.sleep(2);
        check.check().whenComplete((result, error) -> System.out.println("Completed"));
        TimeUnit.SECONDS.sleep(2);
    }
    
    
    @Test
    public void testNoHealthStatuses() {
        Injector injector = Governator.createInjector();
        HealthCheck hc = injector.getInstance(HealthCheck.class);
        
        HealthCheckStatus status = hc.check().join();
        Assert.assertEquals(LifecycleState.Running, status.getState());
    }
    
    @Test
    public void testOneHealthStatus() {
        List<HealthIndicator> indicators = new ArrayList<>();
        Injector injector = Governator.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(HealthIndicatorRegistry.class).toInstance(HealthIndicatorRegistry.from(indicators));
            }
        });
        
        HealthCheck manager = injector.getInstance(HealthCheck.class);
        
        HealthCheckStatus status = manager.check().join();
        Assert.assertEquals(LifecycleState.Running, status.getState());
        
        indicators.add(HealthIndicators.alwaysHealthy("foo"));
        status = manager.check().join();
        Assert.assertEquals(LifecycleState.Running, status.getState());
        
        indicators.add(HealthIndicators.alwaysUnhealthy("foo"));
        status = manager.check().join();
        Assert.assertEquals(LifecycleState.Failed, status.getState());
    }
    
    @Test
    public void testMultipleHealthStatuses() {
        List<HealthIndicator> indicators = new ArrayList<>();
        Injector injector = Governator.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(HealthIndicatorRegistry.class).toInstance(HealthIndicatorRegistry.from(indicators));
            }
        });
        
        HealthCheck manager = injector.getInstance(HealthCheck.class);
        HealthIndicatorRegistry registry = injector.getInstance(HealthIndicatorRegistry.class);
        
        HealthCheckStatus status = manager.check().join();
        Assert.assertEquals(LifecycleState.Running, status.getState());
        Assert.assertEquals(0, status.getIndicators().size());
        
        indicators.add(HealthIndicators.alwaysHealthy("foo"));
        indicators.add(HealthIndicators.alwaysHealthy("bar"));
        status = manager.check().join();
        Assert.assertEquals(LifecycleState.Running, status.getState());
        Assert.assertEquals(2, status.getIndicators().size());
        
        indicators.add(HealthIndicators.alwaysUnhealthy("foo"));
        status = manager.check().join();
        Assert.assertEquals(LifecycleState.Failed, status.getState());
        Assert.assertEquals(3, status.getIndicators().size());
        
        // This will never happen in reality but interesting to test
        indicators.remove(2);
        status = manager.check().join();
        Assert.assertEquals(LifecycleState.Running, status.getState());
        Assert.assertEquals(2, status.getIndicators().size());
    }
}
