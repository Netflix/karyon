package com.netflix.karyon.admin;

import junit.framework.Assert;

import org.junit.Test;

import com.google.inject.multibindings.ProvidesIntoSet;
import com.netflix.governator.DefaultModule;
import com.netflix.governator.Governator;
import com.netflix.governator.LifecycleInjector;
import com.netflix.karyon.HealthCheck;
import com.netflix.karyon.HealthCheckStatus;
import com.netflix.karyon.LifecycleState;
import com.netflix.karyon.healthcheck.HealthIndicator;
import com.netflix.karyon.healthcheck.HealthIndicatorRegistry;
import com.netflix.karyon.healthcheck.HealthIndicators;

public class HealthCheckBindingTest {
    @Test
    public void test() {
        LifecycleInjector injector = Governator.createInjector(
            new DefaultModule() {
                @ProvidesIntoSet
                public HealthIndicator getHealthCheck1() {
                    return HealthIndicators.alwaysHealthy("hc1"); 
                }
                
                @ProvidesIntoSet
                public HealthIndicator getHealthCheck2() {
                    return HealthIndicators.alwaysUnhealthy("hc2"); 
                }
            });
        
        HealthIndicatorRegistry registry = injector.getInstance(HealthIndicatorRegistry.class);
        HealthCheckResource res = injector.getInstance(HealthCheckResource.class);
        
//        HealthIndicatorStatus status1 = injector.getInstance(Key.get(HealthIndicator.class, Names.named("hc1"))).check().join();
//        Assert.assertEquals(true, status1.isHealthy());
//        
//        HealthIndicatorStatus status2 = injector.getInstance(Key.get(HealthIndicator.class, Names.named("hc2"))).check().join();
//        Assert.assertEquals(false, status2.isHealthy());
//        
        HealthCheckStatus status = injector.getInstance(HealthCheck.class).check().join();
        Assert.assertEquals(LifecycleState.Running, status.getState());
        Assert.assertEquals(2, status.getAttributes().size());
    }
}
