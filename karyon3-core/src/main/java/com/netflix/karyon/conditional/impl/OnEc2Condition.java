package com.netflix.karyon.conditional.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.netflix.karyon.KaryonConfiguration;
import com.netflix.karyon.PropertySource;
import com.netflix.karyon.conditional.Condition;
import com.netflix.karyon.conditional.ConditionalOnEc2;

@Singleton
public class OnEc2Condition implements Condition<ConditionalOnEc2> {
    private final PropertySource source;
    private final KaryonConfiguration config;

    @Inject
    public OnEc2Condition(PropertySource source, KaryonConfiguration config) {
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
