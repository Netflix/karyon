/*
 * Copyright 2013 Netflix, Inc.
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */

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
