package com.netflix.karyon.junit;

import org.junit.Rule;
import org.junit.Test;

import com.netflix.governator.LifecycleInjector;
import com.netflix.karyon.KaryonFeatures;

public class TestActiveLifecycleListener {
    @Rule
    public KaryonRule rule = new KaryonRule(this, (karyon) -> {
            karyon.disableFeature(KaryonFeatures.USE_ARCHAIUS);
        });
    
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
