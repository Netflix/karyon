package com.netflix.karyon.conditional.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.netflix.karyon.KaryonAutoContext;
import com.netflix.karyon.conditional.Condition;
import com.netflix.karyon.conditional.ConditionalOnMissingBinding;

@Singleton
public class OnMissingBindingCondition implements Condition<ConditionalOnMissingBinding> {
    private final KaryonAutoContext context;

    @Inject
    public OnMissingBindingCondition(KaryonAutoContext context) {
        this.context = context;
    }
    
    @Override
    public boolean check(ConditionalOnMissingBinding condition) {
        for (String name : condition.value()) {
            try {
                if (context.hasBinding(Class.forName(name, false, ClassLoader.getSystemClassLoader()))) {
                    return false;
                }
            } catch (ClassNotFoundException e) {
            }
        }
        return true;
    }
    
    @Override
    public String toString() {
        return "OnMissingBindingCondition[]";
    }
}
