package com.netflix.karyon.conditional.impl;

import javax.inject.Singleton;

import com.netflix.karyon.conditional.Condition;

@Singleton
public class OnTestNGCondition implements Condition<OnTestNGCondition>{
    @Override
    public boolean check(OnTestNGCondition param) {
        String cmd = System.getProperty("sun.java.command");
        if (cmd == null) {
            return false;
        }
        
        return cmd.startsWith("org.testng.remote.RemoteTestNG");
    }

    @Override
    public String toString() {
        return "OnTestNGCondition[]";
    }
}
