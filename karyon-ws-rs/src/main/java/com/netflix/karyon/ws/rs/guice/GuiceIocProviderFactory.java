package com.netflix.karyon.ws.rs.guice;

import javax.inject.Provider;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.netflix.karyon.ws.rs.IoCProviderFactory;

@Singleton
public class GuiceIocProviderFactory implements IoCProviderFactory {

    private final Injector injector;

    @Inject
    public GuiceIocProviderFactory(Injector injector) {
        this.injector = injector;
    }
    
    @Override
    public <T> Provider<T> getProvider(Class<T> type) {
        return injector.getProvider(type);
    }
}
