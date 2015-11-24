package com.netflix.karyon.conditional.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Conditional that is met if the specified profile is set
 * 
 * @see ProvidesConditionally
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional
public @interface ConditionalOnProfile {
    String[] value();
}
