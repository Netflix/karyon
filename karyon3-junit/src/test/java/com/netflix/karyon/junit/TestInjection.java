package com.netflix.karyon.junit;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import com.google.inject.AbstractModule;
import com.netflix.governator.LifecycleInjector;

public class TestInjection {
    @Rule
    public KaryonRule karyon = new KaryonRule(this);
    
    @Inject
    public String foo;
    
    @Test
    public void testWithStart() {
        karyon.addModules(new AbstractModule() {
            @Override
            protected void configure() {
                bind(String.class).toInstance("Foo");
            }
        });
        
        Assert.assertNull(foo);
        LifecycleInjector injector = karyon.getInjector();
        Assert.assertEquals("Foo", foo);
    }
}
