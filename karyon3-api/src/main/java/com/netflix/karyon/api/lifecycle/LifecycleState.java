package com.netflix.karyon.api.lifecycle;

/**
 * State of an application lifecycle.  State transitions are linear as described in 
 * the following diagram.  
 * 
 * {@code  O -> Starting -> Running -> Stopping -> Stopped -> (O) }
 */
public enum LifecycleState {
    Starting,
    Running,
    Stopping,
    Stopped
}
