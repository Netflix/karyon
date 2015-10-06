package com.netflix.karyon.conditional.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.netflix.karyon.KaryonAutoContext;
import com.netflix.karyon.conditional.Condition;
import com.netflix.karyon.conditional.ConditionalOnBinding;

@Singleton
public class OnBindingCondition implements Condition<ConditionalOnBinding> {
    private final KaryonAutoContext context;

    @Inject
    public OnBindingCondition(KaryonAutoContext context) {
        this.context = context;
    }
    
    @Override
    public boolean check(ConditionalOnBinding condition) {
        for (String name : condition.value()) {
            try {
                if (!context.hasBinding(Class.forName(name, false, ClassLoader.getSystemClassLoader()))) {
                    return false;
                }
            } catch (ClassNotFoundException e) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public String toString() {
        return "OnBindingCondition[]";
    }
}
