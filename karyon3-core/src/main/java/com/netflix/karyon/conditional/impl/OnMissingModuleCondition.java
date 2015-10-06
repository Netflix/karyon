package com.netflix.karyon.conditional.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.netflix.karyon.KaryonAutoContext;
import com.netflix.karyon.conditional.Condition;
import com.netflix.karyon.conditional.ConditionalOnMissingModule;

@Singleton
public class OnMissingModuleCondition implements Condition<ConditionalOnMissingModule>{
    private final KaryonAutoContext context;
    
    @Inject
    public OnMissingModuleCondition(KaryonAutoContext context) {
        this.context = context;
    }
    
    @Override
    public boolean check(ConditionalOnMissingModule param) {
        for (String module : param.value()) {
            if (context.hasModule(module)) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public String toString() {
        return "OnMissingModuleCondition[]";
    }

}
