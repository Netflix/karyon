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
package com.netflix.karyon.server.test.client;

import com.netflix.karyon.server.test.configuration.Configurations;
import com.netflix.karyon.server.test.configuration.KaryonExtensionConfiguration;
import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.event.suite.BeforeSuite;

/**
 * Parses the extension configuration from the arquillian descriptor.
 *
 * @author Jakub Narloch (jmnarloch@gmail.com)
 */
public class KaryonExtensionConfigrationProducer {

    /**
     * The arquillian descriptor.
     */
    @Inject
    private Instance<ArquillianDescriptor> descriptor;

    /**
     * The extension configuration.
     */
    @Inject
    @ApplicationScoped
    private InstanceProducer<KaryonExtensionConfiguration> configuration;

    /**
     * Loads the configuration from the arquillian descriptor.
     *
     * @param event the event
     */
    public void loadExtensionConfiguration(@Observes BeforeSuite event) {

        // loads the properties from the arquillian descriptor
        KaryonExtensionConfiguration config = Configurations.fromDescriptor(descriptor.get()).parse();

        // sets the configuration
        configuration.set(config);
    }
}
