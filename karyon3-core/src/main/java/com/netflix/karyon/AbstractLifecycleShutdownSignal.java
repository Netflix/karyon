package com.netflix.karyon;

import com.netflix.karyon.api.LifecycleShutdownSignal;

public abstract class AbstractLifecycleShutdownSignal implements LifecycleShutdownSignal {

    private final LifecycleManager manager;

    protected AbstractLifecycleShutdownSignal(LifecycleManager manager) {
        this.manager = manager;
    }
    
    protected void shutdown() {
        manager.notifyShutdown();
    }
}
