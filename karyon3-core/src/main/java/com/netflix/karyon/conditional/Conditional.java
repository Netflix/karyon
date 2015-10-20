package com.netflix.karyon.conditional;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Meta annotation for a ConditionalXXX annotation used to indicate which conditions implement
 * processing of the annotation.
 * 
 * @author elandau
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Conditional { 

    /**
     * All {@link Condition}s that must be true in order for the component to be registered.
     */
    Class<? extends Condition<?>>[] value();

}