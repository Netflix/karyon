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

package com.netflix.karyon.finder;

import com.google.inject.Inject;
import com.netflix.config.ConfigurationManager;
import com.netflix.governator.lifecycle.ClasspathScanner;
import com.netflix.karyon.spi.Component;
import com.netflix.karyon.spi.PropertyNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Discovers all the classes that are annotated with {@link com.netflix.karyon.spi.Component}.
 *
 * @author Nitesh Kant
 */
public class ComponentFinder {

    private static final Logger logger = LoggerFactory.getLogger(ComponentFinder.class);

    private ClasspathScanner scanner;

    @Inject
    public ComponentFinder(ClasspathScanner scanner) {
        this.scanner = scanner;
    }

    public Set<Class<?>> findComponents() {

        List<Object> componentClassNames =
                ConfigurationManager.getConfigInstance().getList(PropertyNames.EXPLICIT_COMPONENT_CLASSES_PROP_NAME);

        Set<Class<?>> toReturn = new HashSet<Class<?>>();

        if (!componentClassNames.isEmpty()) {
            for (Object componentClassName : componentClassNames) {
                try {
                    toReturn.add(Class.forName(String.valueOf(componentClassName)));
                } catch (ClassNotFoundException e) {
                    logger.warn(String.format(
                            "Component class %s specified as a property: %s not found. Skipping instantiation of this component.",
                            componentClassName, PropertyNames.EXPLICIT_COMPONENT_CLASSES_PROP_NAME));
                }
            }
            return toReturn;
        }

        Set<Class<?>> potentialMatches = scanner.getClasses();
        if (null == potentialMatches || potentialMatches.isEmpty()) {
            logger.info(
                    "No component classes (Annotated with @Component) found. It is fine if you do not use the annotation model for components or you do not have any.");
            return Collections.emptySet();
        }

        for (Class<?> potentialMatch : potentialMatches) {
            if (potentialMatch.isAnnotationPresent(Component.class)) {
                Component componentAnnotation = potentialMatch.getAnnotation(Component.class);
                String disableProperty = componentAnnotation.disableProperty();
                if (null == disableProperty || disableProperty.isEmpty()) {
                    disableProperty = PropertyNames.COMPONENT_DISABLE_PROP_PEFIX + potentialMatch.getName();
                }

                boolean disable = ConfigurationManager.getConfigInstance().getBoolean(disableProperty, false);
                if (!disable) {
                    toReturn.add(potentialMatch);
                } else {
                    logger.info(String.format("Discovered component %s, you can disable this component from getting discovered by setting a property %s to true",
                                              potentialMatch.getName(), disableProperty));
                }
            }
        }

        return toReturn;
    }
}
