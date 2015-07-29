package com.netflix.karyon.healthcheck;

import junit.framework.Assert;

import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;

public class CompositeHealthCheckTest {
    @Test(expected=Exception.class)
    public void failureOnNoDefinedHealthCheck() {
        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
            }
        });
        
        HealthCheck hc = injector.getInstance(HealthCheck.class);
    }
    
    @Test
    public void successWithSingleHealthCheck() {
        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(HealthCheck.class).toInstance(HealthChecks.alwaysHealthy());
            }
        });
        
        HealthCheck hc = injector.getInstance(HealthCheck.class);
        Assert.assertTrue(hc.check().isHealthy());
        
    }
    
    @Test
    public void successWithSingleAndNamedHealthCheck() {
        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(HealthCheck.class).toInstance(HealthChecks.alwaysHealthy());
                bind(HealthCheck.class).annotatedWith(Names.named("foo")).toInstance(HealthChecks.alwaysUnhealthy());
            }
        });
        
        HealthCheck hc = injector.getInstance(HealthCheck.class);
        Assert.assertTrue(hc.check().isHealthy());
        
    }
    
    @Test
    public void successDefaultCompositeWithSingleNamedHealthCheck() {
        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(HealthCheck.class).to(CompositeHealthCheck.class);
                bind(HealthCheck.class).annotatedWith(Names.named("foo")).toInstance(HealthChecks.alwaysUnhealthy());
            }
        });
        
        HealthCheck hc = injector.getInstance(HealthCheck.class);
        Assert.assertFalse(hc.check().isHealthy());
    }
    
    @Test
    public void successDefaultCompositeWithMultipleNamedHealthCheck() {
        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(HealthCheck.class).to(CompositeHealthCheck.class);
                bind(HealthCheck.class).annotatedWith(Names.named("foo")).toInstance(HealthChecks.alwaysUnhealthy());
                bind(HealthCheck.class).annotatedWith(Names.named("bar")).toInstance(HealthChecks.alwaysUnhealthy());
            }
        });
        
        HealthCheck hc = injector.getInstance(HealthCheck.class);
        HealthStatus status = hc.check();
        Assert.assertFalse(status.isHealthy());
        Assert.assertEquals(2, status.getAttributes().size());
        
    }
}
