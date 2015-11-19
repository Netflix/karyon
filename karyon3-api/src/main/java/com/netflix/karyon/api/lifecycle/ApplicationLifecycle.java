package com.netflix.karyon.api.lifecycle;

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
 * <code>
 * bind(ApplicationLifecycle.class).to(ManualApplicationLifecycle.class);
 * </code>
 * 
 * The inject ApplicationLifecycle and set it to Starting based on your custom logic
 * 
 * <code>
 * {@literal @}Singleton
 * public class Foo {
 *    private final ApplicationLifecycle appLifecycle
 *    {@literal @}Inject
 *    public Foo(ApplicationLifecycle appLifecycle) {
 *        this.appLifecycle = appLifecycle; }
 *    }
 *    
 *    public void someFunctionWithStartupLogic() {
 *        this.appLifecycle.setStarted();
 *    }
 * }
 * </code>
 * 
 * To linked ApplicationLifecycle to both to DI lifecycle and manual state,
 * 
 * <code>
 * bind(ApplicationLifecycle.class).toInstance(new LifecycleListenerApplicationLifecycle(LifecycleState.Starting));
 * </code>
 * 
 * The manually set the lifecycle,
 * 
 * <code>
 * {@literal @}Singleton
 * public class Foo {
 *    private final ApplicationLifecycle appLifecycle;
 *    
 *    {@literal @}Inject
 *    public Foo(ApplicationLifecycle appLifecycle) {
 *        this.appLifecycle = appLifecycle;
 *    }
 *    
 *    public void someFunctionWithStartupLogic() {
 *        this.appLifecycle.setStarted();
 *    }
 * }
 * </code>
 * 
 * @author elandau
 *
 */
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
