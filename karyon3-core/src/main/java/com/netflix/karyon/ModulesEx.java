package com.netflix.karyon;

import com.google.inject.AbstractModule;
import com.google.inject.Module;

final class ModulesEx {
    private static final Module EMPTY_MODULE = new AbstractModule() {
            @Override
            protected void configure() {
            }
        };
        
    public static Module emptyModule() {
        return EMPTY_MODULE;
    }
    
    public static Module fromEagerSingleton(Class<?> type) {
        return new AbstractModule() {
            @Override
            protected void configure() {
                bind(type).asEagerSingleton();
            }
        };
    }
}
