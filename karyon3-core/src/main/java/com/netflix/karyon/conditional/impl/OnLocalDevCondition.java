package com.netflix.karyon.conditional.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.netflix.karyon.KaryonConfiguration;
import com.netflix.karyon.api.PropertySource;
import com.netflix.karyon.conditional.Condition;
import com.netflix.karyon.conditional.ConditionalOnLocalDev;

/**
 * Conditional to add to a module that should be loaded when running in 'local' profile
 * or in a JUnit test (in eclipse or gradle build).
 * 
 * @author elandau
 *
 */
@Singleton
public class OnLocalDevCondition implements Condition<ConditionalOnLocalDev> {
    private final KaryonConfiguration config;
    private final boolean inTest;

    @Inject
    public OnLocalDevCondition(PropertySource source, KaryonConfiguration config, OnJUnitCondition junitCondition) {
        this.config = config;
        this.inTest = isInTest();
    }

    private boolean isInTest() {
        String cmd = System.getProperty("sun.java.command");
        if (cmd == null) {
            return false;
        }
        
        return cmd.startsWith("org.eclipse.jdt.internal.junit.runner") || 
               cmd.contains("Gradle Test Executor");
    }
    
    @Override
    public boolean check(ConditionalOnLocalDev condition) {
        return config.getProfiles().contains("localDev")
                || inTest
                || config.getProfiles().contains("local");  // @deprecated maintain "local" for backward compatibility
    }
    
    @Override
    public String toString() {
        return "OnLocalDevCondition[]";
    }
}
