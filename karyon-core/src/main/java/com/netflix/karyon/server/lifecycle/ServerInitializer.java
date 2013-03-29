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

package com.netflix.karyon.server.lifecycle;

import com.google.common.base.Throwables;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.netflix.karyon.finder.ApplicationFinder;
import com.netflix.karyon.finder.ComponentFinder;
import com.netflix.karyon.spi.ServiceRegistryClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.Set;

/**
 * This class holds the karyon initialization logic that constitutes of the following:
 *
 * <ul>
 <li>Register the application with eureka (if enabled)</li>
 <li>Find all components (annotated with @Component) and instantiate them.</li>
 <li>Find the application class (annotated with @Application) and instantiate it.</li>
 </ul>
 *
 * This also provides a {@link java.io.Closeable#close()} to cleanup upon shutdown.
 *
 * @author Nitesh Kant
 */
public class ServerInitializer implements Closeable {

    private static final Logger logger = LoggerFactory.getLogger(ServerInitializer.class);

    private ServiceRegistryClient serviceRegistryClient;

    private ApplicationFinder applicationFinder;

    private ComponentFinder componentFinder;

    @Inject
    public ServerInitializer(ServiceRegistryClient serviceRegistryClient, ApplicationFinder applicationFinder,
                             ComponentFinder componentFinder) {
        this.serviceRegistryClient = serviceRegistryClient;
        this.applicationFinder = applicationFinder;
        this.componentFinder = componentFinder;
    }

    /**
     * Initializes karyon which is to instantiate applications and components defined in the classpath.
     *
     * @param injector Injector used to initialize <a href="https://github.com/Netflix/governator/">Governator</a>
     */
    public void initialize(Injector injector) {

        Set<Class<?>> components = componentFinder.findComponents();

        for (Class<?> component : components) {
            logger.info(String.format("Initializing component class: %s", component.getName()));
            try {
                injector.getInstance(component);
                logger.info(String.format("Successfully initialized component class: %s", component.getName()));
            } catch (Exception e) {
                logger.error("Failed to initialize component class. Stopping server initialization", e);
                throw Throwables.propagate(e);
            }
        }

        Class<?> application = applicationFinder.findApplication();
        if (null == application) {
            logger.info("No application classes found, this is fine if the application class is not annotated.");
        } else {
            logger.info(String.format("Initializing application class: %s", application.getName()));
            try {
                injector.getInstance(application);
                logger.info(String.format("Successfully initialized application class: %s", application.getName()));
            } catch (Exception e) {
                logger.error("Failed to initialize application class. Stopping server initialization", e);
                throw Throwables.propagate(e);
            }
        }

        serviceRegistryClient.updateStatus(ServiceRegistryClient.ServiceStatus.UP);
    }

    /**
     * Marks the application as down in eureka.
     */
    @Override
    public void close() {
        serviceRegistryClient.updateStatus(ServiceRegistryClient.ServiceStatus.DOWN);
    }
}
