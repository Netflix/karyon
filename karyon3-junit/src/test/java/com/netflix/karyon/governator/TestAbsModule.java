package com.netflix.karyon.governator;

import com.google.inject.AbstractModule;
import com.netflix.governator.annotations.AutoBindSingleton;

@AutoBindSingleton
public class TestAbsModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(TestAbsFromModule.class).asEagerSingleton();
    }
}
