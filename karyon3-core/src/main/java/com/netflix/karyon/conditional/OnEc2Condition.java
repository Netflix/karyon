package com.netflix.karyon.conditional;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.netflix.governator.GovernatorConfiguration;
import com.netflix.governator.auto.Condition;
import com.netflix.governator.auto.PropertySource;

@Singleton
public class OnEc2Condition implements Condition<ConditionalOnEc2> {
    private final PropertySource source;
    private final GovernatorConfiguration config;

    @Inject
    public OnEc2Condition(PropertySource source, GovernatorConfiguration config) {
        this.source = source;
        this.config = config;
    }
    
    @Override
    public boolean check(ConditionalOnEc2 condition) {
        return config.getProfiles().contains("ec2") || source.get("EC2_INSTANCE_ID") != null;
    }
    
    @Override
    public String toString() {
        return "OnCloudCondition[]";
    }
}
