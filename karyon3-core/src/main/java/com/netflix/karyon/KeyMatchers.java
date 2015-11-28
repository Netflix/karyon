package com.netflix.karyon;

import java.lang.annotation.Annotation;

import com.google.inject.Key;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.matcher.Matcher;

/**
 * Utility class for creating matchers for Key<?>
 */
public final class KeyMatchers {
    /**
     * Create a matcher that succeeds if the type contains the specified annotation
     * @param annot Annotation to match
     */
    public static <T extends Key<?>> Matcher<T> annotatedWith(Class<? extends Annotation> annot) {
        return new AbstractMatcher<T>() {
            @Override
            public boolean matches(T t) {
                return null != t.getTypeLiteral().getRawType().getAnnotation(annot);
            }
        };
    }

    /**
     * Create a matcher that succeeds if the key contains the specified qualifier
     * @param annot Annotation to match
     */
    public static <T extends Key<?>> Matcher<T> qualifiedWith(Class<? extends Annotation> annot) {
        return new AbstractMatcher<T>() {
            @Override
            public boolean matches(T t) {
                return t.getAnnotationType() != null && t.getAnnotationType().equals(annot);
            }
        };
    }

    /**
     * Create a matcher that succeeds if the type is a or is a subclass of a specific type
     * @param type 
     */
    public static <T extends Key<?>> Matcher<T> subclassOf(Class<?> type) {
        return new AbstractMatcher<T>() {
            @Override
            public boolean matches(T t) {
                return t.getTypeLiteral().getRawType().isAssignableFrom(type);
            }
        };
    }
}
