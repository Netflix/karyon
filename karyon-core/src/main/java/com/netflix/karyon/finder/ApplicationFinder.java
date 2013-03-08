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

import com.google.common.base.Throwables;
import com.google.inject.Inject;
import com.netflix.governator.annotations.Configuration;
import com.netflix.governator.lifecycle.ClasspathScanner;
import com.netflix.karyon.spi.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static com.netflix.karyon.spi.PropertyNames.DISABLE_APPLICATION_DISCOVERY_PROP_NAME;
import static com.netflix.karyon.spi.PropertyNames.EXPLICIT_APPLICATION_CLASS_PROP_NAME;

/**
 * Discovers all the classes that are annotated with {@link Application}.
 *
 * @author Nitesh Kant
 */
public class ApplicationFinder {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationFinder.class);

    @Configuration(
            value = DISABLE_APPLICATION_DISCOVERY_PROP_NAME,
            documentation = "We will not scan the classpath for @Application annotation classes if set to true."
    )
    private boolean disableAppDiscovery;

    @Configuration(
            value = EXPLICIT_APPLICATION_CLASS_PROP_NAME,
            documentation = "Explicitly defined name of the application class. Used if there are multiple app classes found."
    )
    private String explicitAppClassName;

    private ClasspathScanner scanner;

    @Inject
    public ApplicationFinder(ClasspathScanner scanner) {
        this.scanner = scanner;
    }

    @Nullable
    public Class<?> findApplication() {

        if (disableAppDiscovery) {
            logger.info("Application classes discovery is turned off, not scanning for application classes. Set " +
                        DISABLE_APPLICATION_DISCOVERY_PROP_NAME + " to false to enable this feature.");
            return null;
        }

        Set<Class<?>> potentialApps = scanner.getClasses();
        if (null == potentialApps || potentialApps.isEmpty()) {
            logger.info(
                    "No application classes (Annotated with @Application) found. It is fine if you do not use the annotation model for applications.");
            return null;
        }

        potentialApps = new HashSet<Class<?>>(potentialApps);
        for (Iterator<Class<?>> iterator = potentialApps.iterator(); iterator.hasNext(); ) {
            Class<?> potentialApp = iterator.next();
            if (!potentialApp.isAnnotationPresent(Application.class)) {
                iterator.remove();
            }
        }

        if (potentialApps.isEmpty()) {// If everything got removed.
            logger.info( "No application classes (Annotated with @Application) found. It is fine if you do not use the annotation model for applications.");
            return null;
        }

        if (potentialApps.size() > 1) {
            if (null != explicitAppClassName) {
                try {
                    return Class.forName(explicitAppClassName);
                } catch (ClassNotFoundException e) {
                    logger.error(
                            String.format("Explicit application class %s not found. Terminating application finding.", explicitAppClassName), e);
                    throw Throwables.propagate(e);
                }
            } else {
                logger.warn(String.format("More than one application classes found in the classpath. Classnames: %s. " +
                                          "Only the first application will be instantiated. In order to avoid this random selection,"
                                          + "set a property with name: %s and value as the application class name of the desired"
                                          + "application to be used.", potentialApps, EXPLICIT_APPLICATION_CLASS_PROP_NAME));
            }
        }
        return potentialApps.iterator().next();
    }
}
