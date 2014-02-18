package com.test;

import com.google.inject.Injector;
import com.netflix.governator.guice.LifecycleInjector;
import com.netflix.governator.guice.LifecycleInjectorBuilder;
import com.netflix.karyon.governator.KaryonGovernatorBootstrap;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Nitesh Kant
 */
public class Bootstrap extends KaryonGovernatorBootstrap {

    public static final String BEFORE_INJECTION_CREATION = "beforeInjectorCreation";
    public static final String CREATE_INJECTOR = "createInjector";

    private final Set<String> callbacksInvoked = new HashSet<String>();

    public Bootstrap() {
        super(LifecycleInjector.builder(), "com.netflix");
    }

    @Override
    protected void beforeInjectorCreation(@SuppressWarnings("unused") LifecycleInjectorBuilder builderToBeUsed) {
        callbacksInvoked.add(BEFORE_INJECTION_CREATION);
        super.beforeInjectorCreation(builderToBeUsed);
    }

    @Override
    protected Injector createInjector(LifecycleInjectorBuilder builder) {
        callbacksInvoked.add(CREATE_INJECTOR);
        return super.createInjector(builder);
    }

    public Set<String> getCallbacksInvoked() {
        return callbacksInvoked;
    }
}
