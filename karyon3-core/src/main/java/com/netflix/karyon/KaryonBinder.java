package com.netflix.karyon;

import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matcher;

/**
 * @see KaryonModule
 */
public interface KaryonBinder {
    /**
     * Add an AutoBinder to be used when no binding is provided for a type matched by the matcher
     * 
     * @param matcher
     * @param autoBinder
     */
    <T extends TypeLiteral<?>> void bindAutoBinder(Matcher<T> matcher, AutoBinder autoBinder);
}
