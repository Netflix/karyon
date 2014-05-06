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
package com.netflix.kayron.server.test.configuration;

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.config.descriptor.api.ExtensionDef;
import org.jboss.arquillian.core.spi.Validate;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

/**
 * An utility class for loading and exporting the extension configuration.
 *
 * @author Jakub Narloch (jmnarloch@gmail.com)
 */
public final class Configurations {

    /**
     * The name of the property used for storing the application id.
     */
    private static final String APPLICATION_ID = "applicationId";

    /**
     * The name of the property used for storing the environment.
     */
    private static final String ENVIRONMENT = "environment";

    /**
     * Returns {@link ConfigurationParser} that is capable of loading the configuration from the arquillian descriptor.
     *
     * @param descriptor the arquillian descriptor
     *
     * @return the {@link ConfigurationParser}
     */
    public static ConfigurationParser fromDescriptor(ArquillianDescriptor descriptor) {

        return new ArquillianDescriptorConfigurationParser(descriptor);
    }

    /**
     * Returns {@link ConfigurationParser} that is capable of loading the configuration from the properites file.
     *
     * @param input the input stream of properties file
     *
     * @return the {@link ConfigurationParser}
     */
    public static ConfigurationParser fromProperties(InputStream input) {

        return new PropertiesConfigurationParser(input);
    }

    /**
     * Returns {@link ConfigurationExporter} that can export the configuration into a properties file.
     *
     * @param configuration the configuration
     *
     * @return the {@link ConfigurationParser}
     */
    public static ConfigurationExporter toProperties(KayronExtensionConfiguration configuration) {

        return new PropertiesConfigurationExporter(configuration);
    }

    /**
     * The implementation of {@link ConfigurationParser} that loads the configuration from the descriptor.
     *
     * @author Jakub Narloch (jmnarloch@gmail.com)
     */
    private static class ArquillianDescriptorConfigurationParser implements ConfigurationParser {

        /**
         * Represents the extension configuration name within the arquillian descriptor.
         */
        private static final String KAYRON_EXTENSION = "kayron";

        /**
         * The arquillian descriptor.
         */
        private final ArquillianDescriptor descriptor;

        /**
         * Creates new instance of {@link ArquillianDescriptorConfigurationParser} with given descriptor.
         *
         * @param descriptor the descriptor
         */
        private ArquillianDescriptorConfigurationParser(ArquillianDescriptor descriptor) {

            this.descriptor = descriptor;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public KayronExtensionConfiguration parse() {

            Map<String, String> properties = getExtensionProperties();
            KayronExtensionConfiguration configuration = new KayronExtensionConfiguration();
            configuration.setApplicationId(properties.get(APPLICATION_ID));
            configuration.setEnvironment(properties.get(ENVIRONMENT));
            return configuration;
        }

        /**
         * Retrieves the extension properites.
         *
         * @return the descriptor
         */
        private Map<String, String> getExtensionProperties() {

            if (descriptor != null && descriptor.getExtensions() != null) {
                for (ExtensionDef extension : descriptor.getExtensions()) {
                    if (KAYRON_EXTENSION.equals(extension.getExtensionName())) {
                        return extension.getExtensionProperties();
                    }
                }
            }

            return Collections.emptyMap();
        }
    }

    /**
     * The implementation of {@link ConfigurationParser} that loads the configuration from the descriptor.
     *
     * @author Jakub Narloch (jmnarloch@gmail.com)
     */
    private static class PropertiesConfigurationParser implements ConfigurationParser {

        /**
         * The input stream.
         */
        private final InputStream input;

        /**
         * Creates the instance of {@link PropertiesConfigurationParser} with given input stream.
         *
         * @param input the input stream
         *
         * @throws IllegalArgumentException if {@code input} is {@code null}
         */
        public PropertiesConfigurationParser(InputStream input) {
            Validate.notNull(input, "Parameter 'input' can not be null.");

            this.input = input;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public KayronExtensionConfiguration parse() {

            KayronExtensionConfiguration configuration;

            try {
                configuration = new KayronExtensionConfiguration();

                Properties properties = new Properties();
                properties.load(input);

                configuration.setApplicationId(properties.getProperty(APPLICATION_ID));
                configuration.setEnvironment(properties.getProperty(ENVIRONMENT));

                return configuration;
            } catch (IOException e) {

                throw new RuntimeException("An error occurred when loading the configuration.", e);
            }
        }
    }

    /**
     * The implementation of {@link ConfigurationExporter} that exports the configuration into a properties.
     *
     * @author Jakub Narloch (jmnarloch@gmail.com)
     */
    private static class PropertiesConfigurationExporter implements ConfigurationExporter {

        /**
         * The kayron configuration.
         */
        private final KayronExtensionConfiguration configuration;

        /**
         * Creates new instance of {@link PropertiesConfigurationExporter} with given configuration.
         *
         * @param configuration the configuration
         *
         * @throws IllegalArgumentException if {@code configuration} is {@code null}
         */
        public PropertiesConfigurationExporter(KayronExtensionConfiguration configuration) {
            Validate.notNull(configuration, "Parameter 'configuration' can not be null.");

            this.configuration = configuration;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String exportAsString() {

            try {
                Properties properties = new Properties();
                put(properties, APPLICATION_ID, configuration.getApplicationId());
                put(properties, ENVIRONMENT, configuration.getEnvironment());

                StringWriter writer = new StringWriter();
                properties.store(writer, "kayron-testsuite properties");

                return writer.toString();
            } catch (IOException e) {

                throw new RuntimeException("An error occurred when saving the properties.");
            }
        }

        /**
         * Puts the property into the passed properties if the value of the property is not null.
         *
         * @param properties the properties
         * @param key        the property name
         * @param value      the property value
         */
        private void put(Properties properties, String key, String value) {

            if (value != null) {
                properties.put(key, value);
            }
        }
    }
}
