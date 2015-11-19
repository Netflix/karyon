package com.netflix.karyon.conditional.impl;

import javax.inject.Inject;

import com.google.inject.Singleton;
import com.netflix.karyon.api.PropertySource;
import com.netflix.karyon.conditional.Condition;
import com.netflix.karyon.conditional.ConditionalOnProperty;

@Singleton
public class OnPropertyCondition implements Condition<ConditionalOnProperty> {

    private PropertySource config;

    @Inject
    public OnPropertyCondition(PropertySource config) {
        this.config = config;
    }
    
    @Override
    public boolean check(ConditionalOnProperty condition) {
        String value = config.get(condition.name());
        if (value == null || condition.value() == null) {
            return false;
        }
        return condition.value().equals(value);
    }
    
    @Override
    public String toString() {
        return "OnPropertyCondition[]";
    }
}
