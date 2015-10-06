package com.netflix.karyon.conditional.impl;

import com.google.inject.Singleton;
import com.netflix.karyon.conditional.Condition;
import com.netflix.karyon.conditional.ConditionalOnEnvironment;

@Singleton
public class OnEnvironmentCondition implements Condition<ConditionalOnEnvironment> {
    @Override
    public boolean check(ConditionalOnEnvironment condition) {
        String value = System.getenv(condition.name());
        if (value == null || condition.value() == null) {
            return false;
        }
        return condition.value().equals(value);
    }
    
    @Override
    public String toString() {
        return "OnEnvironmentCondition[]";
    }
}
