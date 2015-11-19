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
import com.google.inject.util.Modules;
import com.netflix.governator.Governator;
import com.netflix.karyon.api.health.HealthCheckStatus;
import com.netflix.karyon.api.health.HealthIndicator;
import com.netflix.karyon.api.health.HealthIndicatorRegistry;
import com.netflix.karyon.api.health.HealthIndicatorStatus;
import com.netflix.karyon.api.health.HealthState;
import com.netflix.karyon.health.HealthCheckImpl;

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
        HealthIndicator check = delayed(100, TimeUnit.MILLISECONDS, HealthIndicatorStatus.healthy("foo"));
        TimeUnit.MILLISECONDS.sleep(200);
        check.check().whenComplete((result, error) -> System.out.println("Completed"));
        TimeUnit.MILLISECONDS.sleep(200);
    }
    
    @Test
    public void testNoHealthStatuses() {
        Injector injector = Governator.createInjector(new KaryonDefaultsModule());
        HealthCheckImpl hc = injector.getInstance(HealthCheckImpl.class);
        
        HealthCheckStatus status = hc.check().join();
        Assert.assertEquals(HealthState.Healthy, status.getState());
    }
    
    @Test
    public void testOneHealthStatus() {
        List<HealthIndicator> indicators = new ArrayList<>();
        Injector injector = Governator.createInjector(
            Modules.override(new KaryonDefaultsModule())
                   .with(new AbstractModule() {
                        @Override
                        protected void configure() {
                            bind(HealthIndicatorRegistry.class).toInstance(HealthIndicatorRegistry.from(indicators));
                        }
                    }));
        
        HealthCheckImpl manager = injector.getInstance(HealthCheckImpl.class);
        
        HealthCheckStatus status = manager.check().join();
        Assert.assertEquals(HealthState.Healthy, status.getState());
        
        indicators.add(HealthIndicator.alwaysHealthy("foo"));
        status = manager.check().join();
        Assert.assertEquals(HealthState.Healthy, status.getState());
        
        indicators.add(HealthIndicator.alwaysUnhealthy("foo"));
        status = manager.check().join();
        Assert.assertEquals(HealthState.Unhealthy, status.getState());
    }
    
    @Test
    public void testMultipleHealthStatuses() {
        List<HealthIndicator> indicators = new ArrayList<>();
        Injector injector = Governator.createInjector(Modules.override(new KaryonDefaultsModule()).with(new AbstractModule() {
            @Override
            protected void configure() {
                bind(HealthIndicatorRegistry.class).toInstance(HealthIndicatorRegistry.from(indicators));
            }
        }));
        
        HealthCheckImpl manager = injector.getInstance(HealthCheckImpl.class);
        HealthIndicatorRegistry registry = injector.getInstance(HealthIndicatorRegistry.class);
        
        HealthCheckStatus status = manager.check().join();
        Assert.assertEquals(HealthState.Healthy, status.getState());
        Assert.assertEquals(0, status.getIndicators().size());
        
        indicators.add(HealthIndicator.alwaysHealthy("foo"));
        indicators.add(HealthIndicator.alwaysHealthy("bar"));
        status = manager.check().join();
        Assert.assertEquals(HealthState.Healthy, status.getState());
        Assert.assertEquals(2, status.getIndicators().size());
        
        indicators.add(HealthIndicator.alwaysUnhealthy("foo"));
        status = manager.check().join();
        Assert.assertEquals(HealthState.Unhealthy, status.getState());
        Assert.assertEquals(3, status.getIndicators().size());
        
        // This will never happen in reality but interesting to test
        indicators.remove(2);
        status = manager.check().join();
        Assert.assertEquals(HealthState.Healthy, status.getState());
        Assert.assertEquals(2, status.getIndicators().size());
    }
}
