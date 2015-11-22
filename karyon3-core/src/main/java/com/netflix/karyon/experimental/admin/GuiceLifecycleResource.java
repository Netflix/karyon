package com.netflix.karyon.experimental.admin;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.netflix.governator.LifecycleManager;
import com.netflix.governator.LifecycleManager.State;
import com.netflix.karyon.admin.AdminService;

@Singleton
@AdminService(name="guice-lifecycle", index="current")
final class GuiceLifecycleResource {
    private LifecycleManager manager;

    public static interface Response {
        LifecycleManager.State getState();
        String getReason();
    }
    
    @Inject
    public GuiceLifecycleResource(LifecycleManager manager) {
        this.manager = manager;
    }
    
    public Response current() {
        return new Response() {
            @Override
            public State getState() {
                return manager.getState();
            }

            @Override
            public String getReason() {
                return manager.getFailureReason() == null 
                    ? ""
                    : manager.getFailureReason().toString();
            }
        };
    }
}
