package com.netflix.karyon;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.netflix.governator.LifecycleListener;

@Singleton
public class LifecycleListenerApplicationLifecycle extends ManualApplicationLifecycleState implements LifecycleListener {

    private LifecycleState injectorState;

    @Inject
    public LifecycleListenerApplicationLifecycle() {
        this(LifecycleState.Started);
    }
    
    public LifecycleListenerApplicationLifecycle(LifecycleState initialState) {
        super(initialState);
        this.injectorState = LifecycleState.Starting;
    }
    
    @Override
    public synchronized void onStarted() {
        injectorState = LifecycleState.Started;
        updateState();
    }

    @Override
    public synchronized void onStopped() {
        injectorState = LifecycleState.Stopped;
        updateState();
    }

    @Override
    public synchronized void onStartFailed(Throwable t) {
        injectorState = LifecycleState.Failed;
        updateState();
    }

    @Override
    protected LifecycleState resolveState() {
        LifecycleState baseState = super.resolveState();
        
        if (injectorState.equals(LifecycleState.Failed) || baseState.equals(LifecycleState.Failed)) {
            return LifecycleState.Failed;
        }
        if (injectorState.equals(LifecycleState.Stopped) || baseState.equals(LifecycleState.Stopped)) {
            return LifecycleState.Stopped;
        }
        if (injectorState.equals(LifecycleState.Starting) || baseState.equals(LifecycleState.Starting)) {
            return LifecycleState.Starting;
        }
        return LifecycleState.Started;
    }
}
