package com.netflix.karyon.archaius;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import com.netflix.config.ConfigurationManager;
import com.netflix.governator.guice.LifecycleInjector;
import com.netflix.karyon.KaryonBootstrap;

public class ArchaiusSuiteTest {
    public static class ModuleInPropertiesFile extends AbstractModule {
        @Override
        protected void configure() {
            bind(String.class).annotatedWith(Names.named("prop1")).toInstance("prop1_value");
        }
    }
    
    public static class ModuleInPropertiesLoader extends AbstractModule {
        @Override
        protected void configure() {
            bind(String.class).annotatedWith(Names.named("prop2")).toInstance("prop2_value");
        }
    }
    
    public static class CustomPropertiesLoader implements PropertiesLoader {
        @Override
        public void load() {
            ConfigurationManager.getConfigInstance().setProperty("karyon.modules." + ModuleInPropertiesLoader.class.getName(), "include");
        }
    }
    
    @KaryonBootstrap(name="foo")
    @ArchaiusBootstrap(overrides={CustomPropertiesLoader.class})
    public static class MyApplicationWithOverrides {
        
    }
    
    @Before
    public void before() {
        ConfigurationManager.getConfigInstance().clear();
    }
    
    @Test
    public void shouldLoadOverrides() {
        Injector injector = LifecycleInjector.bootstrap(MyApplicationWithOverrides.class);
        String value1 = injector.getInstance(Key.get(String.class, Names.named("prop1")));
        String value2 = injector.getInstance(Key.get(String.class, Names.named("prop2")));
        Assert.assertEquals("prop1_value", value1);
        Assert.assertEquals("prop2_value", value2);
    }
    
    @KaryonBootstrap(name="bar")
    @ArchaiusBootstrap()
    public static class MyApplicationWithoutOverrides {
        
    }
    @Test
    public void shouldNotLoadOverrides() {
        Injector injector = LifecycleInjector.bootstrap(MyApplicationWithoutOverrides.class);
        String value1 = injector.getInstance(Key.get(String.class, Names.named("prop1")));
        Binding<String> value2 = injector.getExistingBinding(Key.get(String.class, Names.named("prop2")));
        Assert.assertEquals("prop1_value", value1);
        Assert.assertNull(value2);
    }
}
