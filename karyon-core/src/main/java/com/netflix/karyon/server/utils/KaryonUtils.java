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

package com.netflix.karyon.server.utils;

import com.netflix.config.ConfigurationManager;
import com.netflix.karyon.spi.PropertyNames;

/**
 * Utility methods for karyon
 *
 * @author Nitesh Kant
 */
public class KaryonUtils {

    /**
     * Karyon provides a few core components like eureka which can be enabled/disabled using a property of the form:
     * {@link com.netflix.karyon.spi.PropertyNames#KARYON_PROPERTIES_PREFIX}.[component_name].disable with the value
     * set to true for disabling. By default all core components are enabled.
     *
     *
     * @param componentName Name of the component for which the flag is to be retrieved.
     *
     * @return <code>true</code> if the component is enabled.
     */
    public static boolean isCoreComponentEnabled(String componentName) {
        return !ConfigurationManager.getConfigInstance().getBoolean(getPropname(componentName), false);
    }

    private static String getPropname(String componentName) {
        StringBuilder builder = new StringBuilder();
        builder.append(PropertyNames.KARYON_PROPERTIES_PREFIX);
        builder.append(componentName);
        builder.append(".disable");
        return builder.toString();
    }

}
