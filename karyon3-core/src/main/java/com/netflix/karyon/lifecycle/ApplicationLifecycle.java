package com.netflix.karyon.lifecycle;

import com.google.inject.ImplementedBy;

/**
 * Contract for tracking the application LifecycleState.  The application LifecycleState
 * is used to determine whether an the application is done bootstrapping and is ready
 * to serve traffic.  Application LifecycleState is normally used in combination
 * with HealthCheck to make that final determination.  
 * 
 * Application LifecycleState may be set manually using DefaultApplicationLifecycleState
 * be derived from the DI lifecycle using LifecycleListenerApplicationLifecycle.
 * 
 * By default the application lifecycle is driven entirely by the DI lifecycle and is set
 * to Started once the Injector has been created.
 * 
 * To make lifecycle state entirely manual set the following binding
 * 
 * <pre>
 * {@code
 * bind(ApplicationLifecycle.class).to(ManualApplicationLifecycle.class);
 * }
 * </pre>
 * 
 * The inject ApplicationLifecycle and set it to Starting based on your custom logic
 * 
 * <pre>
 * {@code
 * @Singleton
 * public class Foo {
 *    private final ApplicationLifecycle appLifecycle
 *    @Inject
 *    public Foo(ApplicationLifecycle appLifecycle) {
 *        this.appLifecycle = appLifecycle; }
 *    }
 *    
 *    public void someFunctionWithStartupLogic() {
 *        this.appLifecycle.setStarted();
 *    }
 * }
 * }
 * </pre>
 * 
 * To linked ApplicationLifecycle to both to DI lifecycle and manual state,
 * 
 * <pre>
 * {@code
 * bind(ApplicationLifecycle.class).toInstance(new LifecycleListenerApplicationLifecycle(LifecycleState.Starting));
 * }
 * </pre>
 * 
 * The manually set the lifecycle,
 * 
 * <pre>
 * {@code
 * @Singleton
 * public class Foo {
 *    private final ApplicationLifecycle appLifecycle;
 *    
 *    @Inject
 *    public Foo(ApplicationLifecycle appLifecycle) {
 *        this.appLifecycle = appLifecycle;
 *    }
 *    
 *    public void someFunctionWithStartupLogic() {
 *        this.appLifecycle.setStarted();
 *    }
 * }
 * }
 * </pre>
 * 
 * @author elandau
 *
 */
@ImplementedBy(LifecycleListenerApplicationLifecycle.class)
public interface ApplicationLifecycle {
    public interface Listener {
        void onStateChanged(LifecycleState state);
    }
    
    LifecycleState getState();

    void setState(LifecycleState state);

    void addListener(Listener listener);
    
    void setStarted();
    
    void setStopped();
    
    void setFailed();
}
