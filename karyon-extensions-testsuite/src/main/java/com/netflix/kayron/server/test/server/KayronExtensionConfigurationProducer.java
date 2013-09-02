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
package com.netflix.kayron.server.test.server;

import com.netflix.kayron.server.test.configuration.Configurations;
import com.netflix.kayron.server.test.configuration.KayronExtensionConfiguration;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.event.suite.BeforeSuite;

import java.io.IOException;
import java.io.InputStream;

/**
 * A kayron extension configuration producer, that loads the configuration from the properties files, once per the test
 * suite.
 *
 * @author Jakub Narloch (jmnarloch@gmail.com)
 */
public class KayronExtensionConfigurationProducer {

    /**
     * The properties file name from which the configuration will be loaded.
     */
    private static final String CONFIGURATION_PROPERTIES_FILE = "kayron-testsuite.properties";

    /**
     * The extension configuration.
     */
    @Inject
    @ApplicationScoped
    private InstanceProducer<KayronExtensionConfiguration> configuration;

    /**
     * Loads the properties from the properties file passed with the test deployment.
     *
     * @param event the event
     */
    @SuppressWarnings("PMD.EmptyCatchBlock")
    public void loadExtensionProperties(@Observes BeforeSuite event) {

        KayronExtensionConfiguration config;
        InputStream input = null;

        try {

            input = SecurityActions.getResource(CONFIGURATION_PROPERTIES_FILE);
            config = Configurations.fromProperties(input).parse();
            configuration.set(config);
        } finally {

            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException ex) {
                // ignores the exception
            }
        }
    }
}
