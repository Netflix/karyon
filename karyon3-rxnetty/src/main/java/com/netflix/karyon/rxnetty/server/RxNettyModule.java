package com.netflix.karyon.rxnetty.server;

import com.google.inject.AbstractModule;

/**
 * Internally installed server with default bindings for the RxNettyServerRegistry using
 * Guice bindings as well as the server starter.  This module is auto-installed when create
 * a server with routes defined using the RxNettyModule provided DSL
 * 
 * @author elandau
 */
public final class RxNettyModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(RxNettyHttpServerStarter.class).asEagerSingleton();
        bind(RxNettyHttpServerRegistry.class).to(GuiceRxNettyServerRegistry.class);
    }
    
    @Override
    public boolean equals(Object o) {
        // Is only ever installed internally, so we don't need to check state.
        return o instanceof RxNettyModule;
    }

    @Override
    public int hashCode() {
        return RxNettyModule.class.hashCode();
    }
}
