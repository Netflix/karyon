package com.netflix.karyon;

import java.lang.annotation.Annotation;

import com.google.inject.TypeLiteral;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.matcher.Matcher;

/**
 * Utility class for creating matchers of TypeLiteral
 */
public final class TypeLiteralMatchers {
    /**
     * Create a matcher that succeeds if the type contains the specified annotation
     * @param annot Annotation to match
     */
    public static <T extends TypeLiteral<?>> Matcher<T> annotatedWith(Class<? extends Annotation> annot) {
        return new AbstractMatcher<T>() {
            @Override
            public boolean matches(T t) {
                return null != t.getRawType().getAnnotation(annot);
            }
        };
    }

    /**
     * Create a matcher that succeeds if the type is a or is a subclass of a specific type
     * @param type 
     */
    public static <T extends TypeLiteral<?>> Matcher<T> subclassOf(Class<?> type) {
        return new AbstractMatcher<T>() {
            @Override
            public boolean matches(T t) {
                return t.getRawType().isAssignableFrom(type);
            }
        };
    }
}
