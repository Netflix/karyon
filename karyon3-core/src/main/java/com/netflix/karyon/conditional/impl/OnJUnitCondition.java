package com.netflix.karyon.conditional.impl;

import javax.inject.Singleton;

import com.netflix.karyon.conditional.Condition;

@Singleton
public class OnJUnitCondition implements Condition<OnJUnitCondition>{
    @Override
    public boolean check(OnJUnitCondition param) {
        String cmd = System.getProperty("sun.java.command");
        if (cmd == null) {
            return false;
        }
        
        return cmd.startsWith("org.eclipse.jdt.internal.junit.runner");
        // TODO: Add additional checks for other IDEs
    }
    
    @Override
    public String toString() {
        return "OnJUnitCondition[]";
    }

}
