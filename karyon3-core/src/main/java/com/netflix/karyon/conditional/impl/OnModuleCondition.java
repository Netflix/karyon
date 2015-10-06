package com.netflix.karyon.conditional.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.netflix.karyon.KaryonAutoContext;
import com.netflix.karyon.conditional.Condition;
import com.netflix.karyon.conditional.ConditionalOnModule;

@Singleton
public class OnModuleCondition implements Condition<ConditionalOnModule> {
    private final KaryonAutoContext context;
    
    @Inject
    public OnModuleCondition(KaryonAutoContext context) {
        this.context = context;
    }
    
    @Override
    public boolean check(ConditionalOnModule param) {
        for (Class<?> module : param.value()) {
            if (!context.hasModule(module.getName())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "OnModuleCondition[]";
    }
}
