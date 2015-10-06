package com.netflix.karyon.junit;

import junit.framework.Assert;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.netflix.governator.LifecycleInjector;
import com.netflix.governator.LifecycleListener;
import com.netflix.karyon.Karyon;
import com.netflix.karyon.KaryonModule;

public class KaryonRule extends Karyon implements TestRule {
    
    private LifecycleInjector injector;
    private String name;
    private Object obj;
    
    static class TrackingLifecycleListener implements LifecycleListener {
        private boolean started = false;
        private boolean stopped = false;
        private boolean failed = false;
        
        @Override
        public void onStarted() {
            this.started = true;
        }

        @Override
        public void onStopped() {
            this.stopped = true;
        }

        @Override
        public void onStartFailed(Throwable t) {
            this.stopped = true;
            this.failed = true;
        }
    }
    
    public KaryonRule(Object obj) {
        this(obj, null);
    }

    public KaryonRule(Object obj, KaryonModule module) {
        this.obj = obj;
        this.injector = null;
        
        if (module != null) 
            this.apply(module);
    }

    public LifecycleInjector getInjector() {
        return start();
    }
    
    public LifecycleInjector start() {
        if (this.injector == null) {
            injector = super.start();
            injector.injectMembers(obj);
        }
        else {
            throw new RuntimeException("Already started");
        }
        return injector;
    }
    
    protected void after() {
        System.out.println("Stopping test " + name);
        if (this.injector != null) {
            TrackingLifecycleListener listener = new TrackingLifecycleListener();
            this.injector.addListener(listener);
            injector.shutdown();
            
            Assert.assertTrue(listener.stopped);
        }
    }
    
    protected void before() {
        System.out.println("Starting test " + name);
    }

    @Override
    public Statement apply(Statement base, Description description) {
        this.name = description.getMethodName();
        
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                before();
                try {
                    base.evaluate();
                } finally {
                    after();
                }
            }
        };
    }

}
