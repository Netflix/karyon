package com.netflix.karyon.conditional;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.CreationException;
import com.google.inject.Guice;
import com.netflix.karyon.Karyon;
import com.netflix.karyon.ModuleListProviders;

public class ConditionalOnMissingBindingTest {
    
    @ConditionalOnMissingBinding("com.netflix.karyon.conditional.ConditionalOnMissingBindingTest.DepA")
    public static class DepAImplModule extends AbstractModule {
        @Override
        protected void configure() {
        }
    }
    
    public static interface DepA {
    }
    
    public static class DepAImpl implements DepA {
    }
    
    @Singleton
    public static class Foo {
        @Inject
        Foo(DepA a) {
        }
    }
    
    @Test(expected=CreationException.class)
    public void testNoConditional() {
        Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(Foo.class).asEagerSingleton();
            }
        });
    }
    
    @Test
    public void testWithConditional() {
        Karyon.forApplication("test")
            .addModules(new AbstractModule() {
                @Override
                protected void configure() {
                    bind(Foo.class).asEagerSingleton();
                }
            })
            .addAutoModuleListProvider(ModuleListProviders.forModules(new DepAImplModule()));
    }
}
