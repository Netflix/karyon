package com.netflix.karyon.admin;

import java.util.Map.Entry;

import javax.inject.Named;
import javax.inject.Singleton;

import junit.framework.Assert;

import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Binding;
import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.multibindings.Multibinder;
import com.netflix.governator.Governator;
import com.netflix.governator.LifecycleInjector;
import com.netflix.karyon.health.HealthCheck;
import com.netflix.karyon.health.HealthCheckStatus;
import com.netflix.karyon.health.HealthIndicator;
import com.netflix.karyon.health.HealthIndicatorRegistry;
import com.netflix.karyon.health.HealthIndicators;
import com.netflix.karyon.health.HealthState;
import com.netflix.karyon.lifecycle.LifecycleState;

public class HealthCheckBindingTest {
    @Test
    public void test() {
        LifecycleInjector injector = Governator.createInjector(
            new AbstractModule() {
                @Provides
                @Singleton
                @Named("hc1")
                public HealthIndicator getHealthCheck1() {
                    return HealthIndicators.alwaysHealthy("hc1"); 
                }
                
                @Override
                protected void configure() {
                    Multibinder.newSetBinder(binder(), HealthIndicator.class).addBinding().toInstance(HealthIndicators.alwaysUnhealthy("hc2"));
                    
                }
            });
        
        for (Entry<Key<?>, Binding<?>> binding : injector.getAllBindings().entrySet()) {
            System.out.println(binding.getKey());
        }
        
        HealthIndicatorRegistry registry = injector.getInstance(HealthIndicatorRegistry.class);
        Assert.assertEquals(2, registry.getHealthIndicators().size());
        HealthCheckResource res = injector.getInstance(HealthCheckResource.class);
        
        HealthCheckStatus status = injector.getInstance(HealthCheck.class).check().join();
        Assert.assertEquals(HealthState.Unhealthy, status.getState());
        Assert.assertEquals(2, status.getIndicators().size());
    }
}
