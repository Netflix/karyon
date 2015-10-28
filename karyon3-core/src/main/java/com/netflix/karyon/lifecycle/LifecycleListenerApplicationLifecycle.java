package com.netflix.karyon.lifecycle;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.netflix.governator.LifecycleListener;
import com.netflix.karyon.api.lifecycle.LifecycleState;

@Singleton
public class LifecycleListenerApplicationLifecycle extends ManualApplicationLifecycleState implements LifecycleListener {

    private LifecycleState injectorState;

    @Inject
    public LifecycleListenerApplicationLifecycle() {
        this(LifecycleState.Running);
    }
    
    public LifecycleListenerApplicationLifecycle(LifecycleState initialState) {
        super(initialState);
        this.injectorState = LifecycleState.Starting;
    }
    
    @Override
    public synchronized void onStarted() {
        injectorState = LifecycleState.Running;
        updateState();
    }

    @Override
    public synchronized void onStopped() {
        injectorState = LifecycleState.Stopped;
        updateState();
    }

    @Override
    public synchronized void onStartFailed(Throwable t) {
        injectorState = LifecycleState.Stopped;
        updateState();
    }

    @Override
    protected LifecycleState resolveState() {
        LifecycleState baseState = super.resolveState();
        
        if (injectorState.equals(LifecycleState.Stopped) || baseState.equals(LifecycleState.Stopped)) {
            return LifecycleState.Stopped;
        }
        if (injectorState.equals(LifecycleState.Stopping) || baseState.equals(LifecycleState.Stopping)) {
            return LifecycleState.Stopping;
        }
        if (injectorState.equals(LifecycleState.Starting) || baseState.equals(LifecycleState.Starting)) {
            return LifecycleState.Starting;
        }
        return LifecycleState.Running;
    }
}
