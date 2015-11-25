package com.netflix.karyon.junit;

import org.junit.Rule;
import org.junit.Test;

import com.netflix.karyon.LifecycleInjector;

public class TestActiveLifecycleListener {
    @Rule
    public KaryonRule rule = new KaryonRule(this);
    
    @Test
    public void testWithStart()  {
        System.out.println(rule);
        LifecycleInjector injector = rule.getInjector();
    }
    
    @Test
    public void testWithoutStart()  {
        System.out.println(rule);
    }
}
