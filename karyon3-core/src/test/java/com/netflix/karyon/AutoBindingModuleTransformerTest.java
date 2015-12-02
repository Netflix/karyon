package com.netflix.karyon;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Module;
import com.netflix.karyon.KaryonTypeBindingFactoryTest.Bar;
import com.netflix.karyon.annotations.Priority;
import com.netflix.karyon.spi.AbstractAutoBinder;
import com.netflix.karyon.spi.AutoBinder;

public class AutoBindingModuleTransformerTest {
    
    static abstract class AbstractBarAutoBinder extends AbstractAutoBinder {
        private final String name;

        public AbstractBarAutoBinder(String name) {
            super(KeyMatchers.subclassOf(Bar.class));
            this.name = name;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> Module getModuleForKey(final Key<T> key) {
            return new AbstractModule() {
                @Override
                protected void configure() {
                    bind(key).toInstance((T)new Bar() {
                        @Override
                        public String getName() {
                            return null == key.getAnnotation() ? name : key.getAnnotation().toString() + name;
                        } 
                    });
                }
            };
        }
        
        @Override
        public String toString() {
            return name;
        }
    }
    
    @Priority(1)
    static class BarAutoBinderP1 extends AbstractBarAutoBinder {
        public BarAutoBinderP1() {
            super("p1");
        }
    }
    
    @Priority(2)
    static class BarAutoBinderP2 extends AbstractBarAutoBinder {
        public BarAutoBinderP2() {
            super("p2");
        }
    }
    
    @Test
    public void testSortingOrder() {
        AutoBinder ab1 = new BarAutoBinderP1();
        AutoBinder ab2 = new BarAutoBinderP2();
        
        List<AutoBinder> binders = Arrays.asList(ab1, ab2);
        Collections.sort(binders, AutoBindingModuleTransformer.byPriority);
        
        System.out.println(binders);
        Assert.assertEquals(Arrays.asList(ab2, ab1), binders);
    }
}
