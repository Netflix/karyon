package com.netflix.karyon.healthcheck;

import junit.framework.Assert;

import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;

public class HealthIndicatorTest {
    @Test(expected=Exception.class)
    public void failureOnNoDefinedHealthCheck() {
        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
            }
        });
        
        HealthIndicator hc = injector.getInstance(HealthIndicator.class);
    }
    
    @Test
    public void successWithSingleHealthCheck() {
        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(HealthIndicator.class).toInstance(HealthIndicators.alwaysHealthy("foo"));
            }
        });
        
        HealthIndicator hc = injector.getInstance(HealthIndicator.class);
        Assert.assertTrue(hc.check().join().isHealthy());
        
    }
    
    @Test
    public void successWithSingleAndNamedHealthCheck() {
        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(HealthIndicator.class).toInstance(HealthIndicators.alwaysHealthy("foo"));
                bind(HealthIndicator.class).annotatedWith(Names.named("foo")).toInstance(HealthIndicators.alwaysUnhealthy("foo"));
            }
        });
        
        HealthIndicator hc = injector.getInstance(HealthIndicator.class);
        Assert.assertTrue(hc.check().join().isHealthy());
        
    }
    
    @Test
    public void successDefaultCompositeWithSingleNamedHealthCheck() {
        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(HealthIndicator.class).annotatedWith(Names.named("foo")).toInstance(HealthIndicators.alwaysUnhealthy("foo"));
            }
        });
        
        HealthIndicator hc = injector.getInstance(HealthIndicator.class);
        Assert.assertFalse(hc.check().join().isHealthy());
    }
    
    @Test
    public void successDefaultCompositeWithMultipleNamedHealthCheck() {
        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(HealthIndicator.class).annotatedWith(Names.named("foo")).toInstance(HealthIndicators.alwaysUnhealthy("foo"));
                bind(HealthIndicator.class).annotatedWith(Names.named("bar")).toInstance(HealthIndicators.alwaysUnhealthy("bar"));
            }
        });
        
        HealthIndicator hc = injector.getInstance(HealthIndicator.class);
        HealthIndicatorStatus status = hc.check().join();
        Assert.assertFalse(status.isHealthy());
        Assert.assertEquals(2, status.getAttributes().size());
        
    }
}
