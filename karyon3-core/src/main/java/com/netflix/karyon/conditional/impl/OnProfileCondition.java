package com.netflix.karyon.conditional.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.netflix.karyon.KaryonAutoContext;
import com.netflix.karyon.conditional.Condition;
import com.netflix.karyon.conditional.ConditionalOnProfile;

@Singleton
public class OnProfileCondition implements Condition<ConditionalOnProfile> {

    private KaryonAutoContext context;

    @Inject
    public OnProfileCondition(KaryonAutoContext context) {
        this.context = context;
    }
    
    @Override
    public boolean check(ConditionalOnProfile condition) {
        if (condition.matchAll()) {
            for (String profile : condition.value()) {
                if (!context.hasProfile(profile))
                    return false;
            }
            return true;
        }
        else {
            for (String profile : condition.value()) {
                if (!context.hasProfile(profile))
                    return true;
            }
            return false;
        }
    }
    
    @Override
    public String toString() {
        return "OnProfileCondition[]";
    }
}
