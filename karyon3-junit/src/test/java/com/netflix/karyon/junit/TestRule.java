package com.netflix.karyon.junit;

import org.junit.Rule;
import org.junit.Test;

import com.netflix.governator.LifecycleInjector;

public class TestRule {
    @Rule
    public KaryonRule rule = new KaryonRule(this);
    
    @Test
    public void testWithStart() throws Exception {
        LifecycleInjector injector = rule.getInjector();
    }
    
    @Test
    public void testWithoutStart() throws Exception {
    }
}
