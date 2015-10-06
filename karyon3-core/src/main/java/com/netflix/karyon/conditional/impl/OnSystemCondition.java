package com.netflix.karyon.conditional.impl;

import com.google.inject.Singleton;
import com.netflix.karyon.conditional.Condition;
import com.netflix.karyon.conditional.ConditionalOnSystem;

@Singleton
public class OnSystemCondition implements Condition<ConditionalOnSystem> {
    @Override
    public boolean check(ConditionalOnSystem condition) {
        String value = System.getProperty(condition.name());
        if (value == null || condition.value() == null) {
            return false;
        }
        return condition.value().equals(value);
    }
    
    @Override
    public String toString() {
        return "OnSystemCondition[]";
    }
}
