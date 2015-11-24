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
     * @param matcher  Logic for matching an unbound TypeLiteral.  See {@link TypeLiteralMatchers} for
     *  pre-defined matchers.
     * @param autoBinder AutoBinder that will created the bindings for any type matched by the matcher
     */
    <T extends TypeLiteral<?>> void bindAutoBinder(Matcher<T> matcher, AutoBinder autoBinder);
}
