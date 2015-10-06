package com.netflix.karyon.junit;

import org.junit.Rule;
import org.junit.Test;

import com.google.inject.AbstractModule;
import com.netflix.governator.LifecycleInjector;
import com.netflix.karyon.KaryonFeatures;

public class TestDefaultRule {
    @Rule
    public KaryonRule rule = new KaryonRule(this, (karyon) -> {
        karyon.disableFeature(KaryonFeatures.USE_ARCHAIUS);
    });    
    
    @Test
    public void testWithStart() {
        rule.addModules(new AbstractModule() {
            @Override
            protected void configure() {
            }
        });
        
        LifecycleInjector injector = rule.getInjector();
    }
}
