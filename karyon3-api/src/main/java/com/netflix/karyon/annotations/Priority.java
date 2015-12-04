package com.netflix.karyon.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use Priority with classes that are auto-loaded (such as AutoBinder) to control
 * the order in which these auto-loaded classes are called.  
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Priority {
    /**
     * @return The priority.  Higher priorities are called first
     */
    int value();
}