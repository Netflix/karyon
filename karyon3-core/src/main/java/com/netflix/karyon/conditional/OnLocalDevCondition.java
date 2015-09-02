package com.netflix.karyon.conditional;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.netflix.governator.GovernatorConfiguration;
import com.netflix.governator.auto.Condition;
import com.netflix.governator.auto.PropertySource;
import com.netflix.governator.auto.conditions.OnJUnitCondition;

@Singleton
public class OnLocalDevCondition implements Condition<ConditionalOnLocalDevTest> {
    private final GovernatorConfiguration config;
    private final OnJUnitCondition junitCondition;

    @Inject
    public OnLocalDevCondition(PropertySource source, GovernatorConfiguration config, OnJUnitCondition junitCondition) {
        this.config = config;
        this.junitCondition = junitCondition;
    }
    
    @Override
    public boolean check(ConditionalOnLocalDevTest condition) {
        return config.getProfiles().contains("local") || junitCondition.check(null);
    }
    
    @Override
    public String toString() {
        return "OnLocalDevCondition[]";
    }
}
