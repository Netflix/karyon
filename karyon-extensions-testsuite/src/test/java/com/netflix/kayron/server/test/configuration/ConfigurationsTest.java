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
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests the {@link Configurations} class.
 *
 * @author Jakub Narloch (jmnarloch@gmail.com)
 */
public class ConfigurationsTest {

    /**
     * Tests the {@link Configurations#fromDescriptor(ArquillianDescriptor)} method.
     */
    @Test
    public void shouldReadConfigurationFromArquillianDescriptor() {

        // given
        ArquillianDescriptor descriptor = Descriptors.importAs(ArquillianDescriptor.class)
                .fromFile(new File("src/test/resources", "arquillian.xml"));

        // when
        KayronExtensionConfiguration configuration = Configurations.fromDescriptor(descriptor).parse();

        // then
        assertNotNull("The configuration hasn't been loaded.", configuration);
        assertEquals("The property value has invalid value.", "applicationId", configuration.getApplicationId());
        assertEquals("The property value has invalid value.", "environment", configuration.getEnvironment());
    }

    /**
     * Tests the {@link Configurations#fromProperties(InputStream)} method.
     *
     * @throws IOException if any error occurs
     */
    @Test
    @SuppressWarnings("PMD.EmptyCatchBlock")
    public void shouldReadConfigurationFromProperties() throws IOException {

        InputStream input = null;
        try {
            // given
            input = new FileInputStream(new File("src/test/resources", "kayron-testsuite.properties"));

            // when
            KayronExtensionConfiguration configuration = Configurations.fromProperties(input).parse();

            // then
            assertNotNull("The configuration hasn't been loaded.", configuration);
            assertEquals("The property value has invalid value.", "applicationId", configuration.getApplicationId());
            assertEquals("The property value has invalid value.", "environment", configuration.getEnvironment());
        } finally {

            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    // ignores exception
                }
            }
        }
    }

    /**
     * Tests the {@link Configurations#toProperties(KayronExtensionConfiguration)} method.
     *
     * @throws IOException if any error occurs
     */
    @Test
    public void shouldExportConfigurationToProperties() throws IOException {

        // given
        KayronExtensionConfiguration configuration = new KayronExtensionConfiguration();
        configuration.setApplicationId("applicationId");
        configuration.setEnvironment("environment");

        // when
        String properties = Configurations.toProperties(configuration).exportAsString();

        // then
        StringReader reader = new StringReader(properties);
        Properties props = new Properties();
        props.load(reader);

        assertEquals("The property value has invalid value.", "applicationId", props.getProperty("applicationId"));
        assertEquals("The property value has invalid value.", "environment", props.getProperty("environment"));
    }
}
