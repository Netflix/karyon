package com.test;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Nitesh Kant
 */
public class RegistrationSequence {

    private static Set<Class<?>> initializationOrder = Collections.synchronizedSet(new LinkedHashSet<Class<?>>());

    public static void addClass(Class<?> initialized) {
        initializationOrder.add(initialized);
    }

    public static void reset() {
        initializationOrder.clear();
    }

    public static boolean isBefore(Class<?> first, Class<?> second) {
        if (!initializationOrder.contains(first) || !initializationOrder.contains(first)) {
            return false;
        }

        for (Class<?> aClass : initializationOrder) {
            if (aClass.equals(second)) {
                return false;
            } else if (aClass.equals(first)) {
                return true;
            }
        }
        return false;
    }

    public static boolean contains(Class<?> aClass) {
        return initializationOrder.contains(aClass);
    }
}
