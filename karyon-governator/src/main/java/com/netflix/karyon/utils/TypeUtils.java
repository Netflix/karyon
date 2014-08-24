package com.netflix.karyon.utils;

import java.lang.annotation.Annotation;

import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.util.Types;

/**
 * Set of utility methods.
 *
 * @author Tomasz Bak
 */
public final class TypeUtils {
    private TypeUtils() {
    }

    @SuppressWarnings("unchecked")
    public static <T> Key<T> keyFor(Class<?> type, Class<?> typeArg1, Class<?> typeArg2, Annotation annotation) {
        TypeLiteral<T> typeLiteral = (TypeLiteral<T>) TypeLiteral.get(Types.newParameterizedType(type, typeArg1, typeArg2));
        return Key.get(typeLiteral, annotation);
    }
}
