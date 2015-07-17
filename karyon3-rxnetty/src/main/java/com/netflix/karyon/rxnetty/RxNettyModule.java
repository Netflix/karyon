package com.netflix.karyon.rxnetty;

import com.google.inject.AbstractModule;

public class RxNettyModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(RxNettyServerStarter.class).asEagerSingleton();
        bind(RxNettyServerRegistry.class).to(GuiceRxNettyServerRegistry.class);
    }
}
