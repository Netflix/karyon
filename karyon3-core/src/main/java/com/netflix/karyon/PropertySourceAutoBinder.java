package com.netflix.karyon;

import com.google.inject.Binder;
import com.google.inject.Key;
import com.netflix.karyon.spi.AutoBinder;

public class PropertySourceAutoBinder implements AutoBinder {
    @Override
    public <T> boolean configure(Binder binder, Key<T> key) {
        binder.bind(PropertySource.class).toInstance(DefaultPropertySource.INSTANCE);
        return true;
    }
}
