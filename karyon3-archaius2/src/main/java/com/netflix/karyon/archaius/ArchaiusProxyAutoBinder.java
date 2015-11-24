package com.netflix.karyon.archaius;

import javax.inject.Inject;
import javax.inject.Provider;

import com.google.inject.Binder;
import com.google.inject.Key;
import com.netflix.archaius.ConfigProxyFactory;
import com.netflix.karyon.spi.AutoBinder;

/**
 * Autobinder to create proxies for any interface containing the archaius
 * annotations
 */
final class ArchaiusProxyAutoBinder implements AutoBinder {
    @Override
    public <T> boolean configure(Binder binder, Key<T> key) {
        binder.bind(key).toProvider(new Provider<T>() {
            @Inject
            ConfigProxyFactory factory;
            
            @SuppressWarnings("unchecked")
            @Override
            public T get() {
                return (T) factory.newProxy(key.getTypeLiteral().getRawType());
            }
        });
        return true;
    }
}
