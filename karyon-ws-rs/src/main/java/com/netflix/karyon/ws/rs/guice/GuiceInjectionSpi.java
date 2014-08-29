package com.netflix.karyon.ws.rs.guice;

import javax.inject.Provider;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.netflix.karyon.ws.rs.InjectionSpi;

@Singleton
public class GuiceInjectionSpi implements InjectionSpi {

    private final Injector injector;

    @Inject
    public GuiceInjectionSpi(Injector injector) {
        this.injector = injector;
    }
    
    @Override
    public <T> Provider<T> getProvider(Class<T> type) {
        return injector.getProvider(type);
    }

    @Override
    public <T> Provider<T> getProvider(Class<T> type, InjectionSpi scoped) {
        return injector.getProvider(type);
    }
    
}
