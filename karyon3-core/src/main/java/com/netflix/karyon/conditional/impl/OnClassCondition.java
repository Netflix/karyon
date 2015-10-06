package com.netflix.karyon.conditional.impl;

import com.google.inject.Singleton;
import com.netflix.karyon.conditional.Condition;
import com.netflix.karyon.conditional.ConditionalOnClass;

@Singleton
public class OnClassCondition implements Condition<ConditionalOnClass> {
    @Override
    public boolean check(ConditionalOnClass condition) {
        for (String name : condition.value()) {
            try {
                Class.forName(name, false, ClassLoader.getSystemClassLoader());
            } catch (ClassNotFoundException e) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public String toString() {
        return "OnClassCondition[]";
    }
}
