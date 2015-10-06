package com.netflix.karyon.conditional;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.netflix.karyon.conditional.impl.OnSystemCondition;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnSystemCondition.class)
public @interface ConditionalOnSystem {
    String name();
    String value();
}
