package com.netflix.karyon.conditional.impl;

import java.lang.annotation.Annotation;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.netflix.karyon.KaryonAutoContext;
import com.netflix.karyon.conditional.Condition;
import com.netflix.karyon.conditional.ConditionalOnMissingBinding;

/**
 * Use this condition when providing the binding for a specific context.  This binding
 * is usually specified with additional conditions such as {@literal @ConditionalOnProfile}
 * 
 */
@Singleton
public class OnMissingBindingCondition implements Condition<ConditionalOnMissingBinding> {
    private final KaryonAutoContext context;

    @Inject
    public OnMissingBindingCondition(KaryonAutoContext context) {
        this.context = context;
    }
    
    @Override
    public boolean check(ConditionalOnMissingBinding condition) {
        try {
            Class<?> type = Class.forName(condition.value(), false, ClassLoader.getSystemClassLoader());
            Class<? extends Annotation> annot = condition.qualifier().isEmpty() 
                        ? null 
                        : (Class<? extends Annotation>)Class.forName(condition.qualifier(), false, ClassLoader.getSystemClassLoader());
            
            return  context.hasInjectionPoint(type, annot) 
                && !context.hasBinding(type, annot);
        } catch (ClassNotFoundException e) {
        }
        return false;
    }
    
    @Override
    public String toString() {
        return "OnMissingBindingCondition[]";
    }
}
