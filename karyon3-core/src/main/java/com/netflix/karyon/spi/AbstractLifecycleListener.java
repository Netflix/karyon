package com.netflix.karyon.spi;

public abstract class AbstractLifecycleListener implements LifecycleListener {
    @Override
    public void onStopped() {
    }

    @Override
    public void onStarted() {
    }

    @Override
    public void onStartFailed(Throwable t) {
    }
}
