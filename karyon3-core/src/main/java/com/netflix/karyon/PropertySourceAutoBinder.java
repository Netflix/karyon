package com.netflix.karyon;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Module;
import com.netflix.karyon.spi.AbstractAutoBinder;

public class PropertySourceAutoBinder extends AbstractAutoBinder {
    
    public PropertySourceAutoBinder() {
        super(KeyMatchers.subclassOf(PropertySource.class));
    }

    @Override
    public <T> Module getModuleForKey(Key<T> key) {
        return new AbstractModule() {
            @Override
            protected void configure() {
                bind(PropertySource.class).toInstance(DefaultPropertySource.INSTANCE);
            }
        };
    }
}
