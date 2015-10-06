package com.netflix.karyon.conditional.impl;

import javax.inject.Singleton;

import com.netflix.karyon.conditional.Condition;
import com.netflix.karyon.conditional.ConditionalOnMacOS;

@Singleton
public class OnMacOSCondition implements Condition<ConditionalOnMacOS>{
    @Override
    public boolean check(ConditionalOnMacOS param) {
        return "Mac OS X".equals(System.getProperty("os.name"));
    }
    
    @Override
    public String toString() {
        return "OnMacOSCondition[]";
    }

}
