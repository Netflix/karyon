package com.netflix.karyon.admin;

import javax.inject.Named;

import junit.framework.Assert;

import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.name.Names;
import com.netflix.governator.Governator;
import com.netflix.governator.LifecycleInjector;
import com.netflix.karyon.healthcheck.CompositeHealthCheck;
import com.netflix.karyon.healthcheck.HealthCheck;
import com.netflix.karyon.healthcheck.HealthCheckRegistry;
import com.netflix.karyon.healthcheck.HealthChecks;
import com.netflix.karyon.healthcheck.HealthStatus;

public class HealthCheckBindingTest {
    @Test
    public void test() {
        LifecycleInjector injector = Governator.createInjector(
            new AbstractModule() {
                @Provides
                @Named("hc1")
                public HealthCheck getHealthCheck1() {
                    return HealthChecks.alwaysHealthy(); 
                }
                
                @Provides
                @Named("hc2")
                public HealthCheck getHealthCheck2() {
                    return HealthChecks.alwaysUnhealthy(); 
                }

                @Override
                protected void configure() {
                    bind(HealthCheck.class).to(CompositeHealthCheck.class);
                }
                
            });
        
        HealthCheckRegistry registry = injector.getInstance(HealthCheckRegistry.class);
        HealthCheckResource res = injector.getInstance(HealthCheckResource.class);
        
        HealthStatus status1 = injector.getInstance(Key.get(HealthCheck.class, Names.named("hc1"))).check().join();
        Assert.assertEquals(true, status1.isHealthy());
        
        HealthStatus status2 = injector.getInstance(Key.get(HealthCheck.class, Names.named("hc2"))).check().join();
        Assert.assertEquals(false, status2.isHealthy());
        
        HealthStatus status = injector.getInstance(Key.get(HealthCheck.class)).check().join();
        Assert.assertEquals(false, status.isHealthy());
        Assert.assertEquals(2, status.getAttributes().size());
    }
}
