package com.netflix.karyon.governator;

import junit.framework.Assert;

import org.junit.Rule;
import org.junit.Test;

import com.netflix.karyon.governator.ignore.TestAbsToIgnore;
import com.netflix.karyon.junit.KaryonRule;

public class AutoBindModuleTest {
    @Rule
    public KaryonRule karyon = new KaryonRule(this);
    
    @Test
    public void shouldAddAllAbs() {
        karyon
            .apply(new AutoBindModule()
                .includePackages("com.netflix.karyon.governator"))
            .start();
        
        Assert.assertTrue(karyon.didInject(TestAbs.class));
        Assert.assertTrue(karyon.didInject(TestAbsFromModule.class));
        Assert.assertTrue(karyon.didInject(TestAbsToIgnore.class));
    }
    
    @Test
    public void shouldIgnoreClass() {
        karyon
            .apply(new AutoBindModule()
                .includePackages("com.netflix.karyon.governator")
                .ignoreClasses(TestAbsToIgnore.class))
            .start();
        
        Assert.assertTrue(karyon.didInject(TestAbs.class));
        Assert.assertTrue(karyon.didInject(TestAbsFromModule.class));
        Assert.assertFalse(karyon.didInject(TestAbsToIgnore.class));
    }
    
    @Test
    public void shouldIgnorePackage() {
        karyon
            .apply(new AutoBindModule()
                .includePackages("com.netflix.karyon.governator")
                .ignorePackages("com.netflix.karyon.governator.ignore"))
            .start();
        
        Assert.assertTrue(karyon.didInject(TestAbs.class));
        Assert.assertTrue(karyon.didInject(TestAbsFromModule.class));
        Assert.assertFalse(karyon.didInject(TestAbsToIgnore.class));
    }
}
