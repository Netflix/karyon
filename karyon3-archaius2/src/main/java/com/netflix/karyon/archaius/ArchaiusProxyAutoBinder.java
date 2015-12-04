package com.netflix.karyon.archaius;

import javax.inject.Inject;
import javax.inject.Provider;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Module;
import com.netflix.archaius.ConfigProxyFactory;
import com.netflix.archaius.annotations.Configuration;
import com.netflix.karyon.KeyMatchers;
import com.netflix.karyon.spi.AbstractAutoBinder;

/**
 * Autobinder to create proxies for any unbound interface containing the archaius
 * Configuration annotation.  Note that the auto-binder will not be called if a binding
 * for the interface exists.
 */
final public class ArchaiusProxyAutoBinder extends AbstractAutoBinder {
    public ArchaiusProxyAutoBinder() {
        super(KeyMatchers.annotatedWith(Configuration.class));
    }

    @Override
    public <T> Module getModuleForKey(final Key<T> key) {
        return new AbstractModule() {
            @Override
            protected void configure() {
                bind(key).toProvider(new Provider<T>() {
                    @Inject
                    ConfigProxyFactory factory;
                    
                    @SuppressWarnings("unchecked")
                    @Override
                    public T get() {
                        return (T) factory.newProxy(key.getTypeLiteral().getRawType());
                    }
                });
            }
        };
    }
}
