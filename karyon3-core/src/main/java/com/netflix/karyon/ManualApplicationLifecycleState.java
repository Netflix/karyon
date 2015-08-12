package com.netflix.karyon;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * 
 * @author elandau
 */
@Singleton
public class ManualApplicationLifecycleState implements ApplicationLifecycle {

    private List<Listener> listeners = new ArrayList<>();
    private LifecycleState state;
    private LifecycleState cachedState;
    
    @Inject
    public ManualApplicationLifecycleState() {
        this(LifecycleState.Starting);
    }
    
    public ManualApplicationLifecycleState(LifecycleState initialState) {
        this.state = initialState;
        this.cachedState = null;
    }
    
    @Override
    public LifecycleState getState() {
        return cachedState == null ? LifecycleState.Starting : cachedState;
    }

    @Override
    public synchronized void setState(LifecycleState state) {
        this.state = state;
        updateState();
    }
    
    protected LifecycleState resolveState() {
        return this.state;
    }
    
    protected void updateState() {
        LifecycleState state = resolveState();
        if (!state.equals(cachedState)) {
            cachedState = state;
            
            for (Listener listener : listeners) {
                listener.onStateChanged(cachedState);
            }
        }
    }

    @Override
    public void setStarted() {
        setState(LifecycleState.Started);
    }

    @Override
    public void setStopped() {
        setState(LifecycleState.Stopped);
    }

    @Override
    public void setFailed() {
        setState(LifecycleState.Failed);
    }

    @Override
    public synchronized void addListener(Listener listener) {
        listeners.add(listener);
        listener.onStateChanged(getState());
    }
}
