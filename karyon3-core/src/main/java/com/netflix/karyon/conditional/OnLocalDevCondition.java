package com.netflix.karyon.conditional;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.netflix.governator.GovernatorConfiguration;
import com.netflix.governator.auto.Condition;
import com.netflix.governator.auto.PropertySource;

@Singleton
public class OnLocalDevCondition implements Condition<ConditionalOnLocalDev> {
    private final GovernatorConfiguration config;

    @Inject
    public OnLocalDevCondition(PropertySource source, GovernatorConfiguration config) {
        this.config = config;
    }
    
    @Override
    public boolean check(ConditionalOnLocalDev condition) {
        return config.getProfiles().contains("local");
    }
    
    @Override
    public String toString() {
        return "OnLocalDevCondition[]";
    }
}
