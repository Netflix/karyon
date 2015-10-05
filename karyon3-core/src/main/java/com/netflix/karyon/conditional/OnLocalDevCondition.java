package com.netflix.karyon.conditional;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.netflix.governator.GovernatorConfiguration;
import com.netflix.governator.auto.Condition;
import com.netflix.governator.auto.PropertySource;
import com.netflix.governator.auto.conditions.OnJUnitCondition;

/**
 * Conditional to add to a module that should be loaded when running in 'local' profile
 * or in a JUnit test (in eclipse or gradle build).
 * 
 * @author elandau
 *
 */
@Singleton
public class OnLocalDevCondition implements Condition<ConditionalOnLocalDev> {
    private final GovernatorConfiguration config;
    private final boolean inTest;

    @Inject
    public OnLocalDevCondition(PropertySource source, GovernatorConfiguration config, OnJUnitCondition junitCondition) {
        this.config = config;
        this.inTest = isInTest();
    }

    private boolean isInTest() {
        String cmd = System.getProperty("sun.java.command");
        if (cmd == null) {
            return false;
        }
        
        return cmd.startsWith("org.eclipse.jdt.internal.junit.runner") ||
               cmd.startsWith("com.intellij.rt.execution.application.AppMain") ||
               cmd.contains("Gradle Test Executor");
    }
    
    @Override
    public boolean check(ConditionalOnLocalDev condition) {
        return config.getProfiles().contains("local") || inTest;
    }
    
    @Override
    public String toString() {
        return "OnLocalDevCondition[]";
    }
}
