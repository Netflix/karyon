package com.netflix.karyon.healthcheck;

import junit.framework.Assert;

import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.name.Names;
import com.google.inject.util.Modules;
import com.netflix.governator.Governator;
import com.netflix.karyon.KaryonDefaultsModule;
import com.netflix.karyon.api.health.HealthCheckStatus;
import com.netflix.karyon.api.health.HealthIndicator;
import com.netflix.karyon.api.health.HealthState;
import com.netflix.karyon.health.HealthCheckImpl;

public class HealthIndicatorTest {
    @Test
    public void failureOnNoDefinedHealthCheck() {
        Injector injector = Governator.createInjector(Modules.override(new KaryonDefaultsModule()).with(new AbstractModule() {
            @Override
            protected void configure() {
            }
        }));
        
        HealthCheckImpl hc = injector.getInstance(HealthCheckImpl.class);
        HealthCheckStatus status = hc.check().join();
        Assert.assertEquals(HealthState.Healthy, hc.check().join().getState());
        Assert.assertEquals(0, status.getIndicators().size());
    }
    
    @Test
    public void successWithSingleHealthCheck() {
        Injector injector = Governator.createInjector(Modules.override(new KaryonDefaultsModule()).with(new AbstractModule() {
            @Override
            protected void configure() {
                bind(HealthIndicator.class).toInstance(HealthIndicator.alwaysHealthy("foo"));
            }
        }));
        
        HealthCheckImpl hc = injector.getInstance(HealthCheckImpl.class);
        HealthCheckStatus status = hc.check().join();
        Assert.assertEquals(HealthState.Healthy, hc.check().join().getState());
        Assert.assertEquals(1, status.getIndicators().size());
    }
    
    @Test
    public void successWithSingleAndNamedHealthCheck() {
        Injector injector = Governator.createInjector(Modules.override(new KaryonDefaultsModule()).with(new AbstractModule() {
            @Override
            protected void configure() {
                bind(HealthIndicator.class).toInstance(HealthIndicator.alwaysHealthy("foo"));
                bind(HealthIndicator.class).annotatedWith(Names.named("foo")).toInstance(HealthIndicator.alwaysUnhealthy("foo"));
            }
        }));
        
        HealthCheckImpl hc = injector.getInstance(HealthCheckImpl.class);
        HealthCheckStatus status = hc.check().join();
        Assert.assertEquals(HealthState.Unhealthy, hc.check().join().getState());
        Assert.assertEquals(2, status.getIndicators().size());
        
    }
    
    @Test
    public void successDefaultCompositeWithSingleNamedHealthCheck() {
        Injector injector = Governator.createInjector(Modules.override(new KaryonDefaultsModule()).with(new AbstractModule() {
            @Override
            protected void configure() {
                bind(HealthIndicator.class).annotatedWith(Names.named("foo")).toInstance(HealthIndicator.alwaysUnhealthy("foo"));
            }
        }));
        HealthCheckImpl hc = injector.getInstance(HealthCheckImpl.class);
        HealthCheckStatus status = hc.check().join();
        Assert.assertEquals(HealthState.Unhealthy, status.getState());
        Assert.assertEquals(1, status.getIndicators().size());
    }
    
    @Test
    public void successDefaultCompositeWithMultipleNamedHealthCheck() {
        Injector injector = Governator.createInjector(Modules.override(new KaryonDefaultsModule()).with(new AbstractModule() {
            @Override
            protected void configure() {
                bind(HealthIndicator.class).annotatedWith(Names.named("foo")).toInstance(HealthIndicator.alwaysUnhealthy("foo"));
                bind(HealthIndicator.class).annotatedWith(Names.named("bar")).toInstance(HealthIndicator.alwaysUnhealthy("bar"));
            }
        }));
        
        HealthCheckImpl hc = injector.getInstance(HealthCheckImpl.class);
        HealthCheckStatus status = hc.check().join();
        Assert.assertEquals(HealthState.Unhealthy, hc.check().join().getState());
        Assert.assertEquals(2, status.getIndicators().size());
        
    }
}
