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

import com.netflix.kayron.server.test.RunInKaryon;
import org.jboss.arquillian.test.spi.event.suite.BeforeClass;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Tests the {@link KayronServerInitializer} class.
 *
 * @author Jakub Narloch (jmnarloch@gmail.com)
 */
public class KayronServerInitializerTest {

    /**
     * Represents the kayron property name.
     */
    private static final String APPLICATION_ID_PROPERTY_NAME = "archaius.deployment.applicationId";

    /**
     * Represents the kayron property name.
     */
    private static final String ENVIRONMENT_PROPERTY_NAME = "archaius.deployment.environment";

    /**
     * Represents the instance of the tested class.
     */
    private KayronServerInitializer instance;

    /**
     * Stores the system properties between the test invocations.
     */
    private Properties systemProperties;

    /**
     * Sets up the test environment.
     */
    @Before
    public void setUp() {

        // given
        instance = new KayronServerInitializer();

        // stores the properties between the test execution
        systemProperties = System.getProperties();

        Properties testScopeProperties = new Properties(systemProperties);
        testScopeProperties.remove(APPLICATION_ID_PROPERTY_NAME);
        testScopeProperties.remove(ENVIRONMENT_PROPERTY_NAME);
        System.setProperties(testScopeProperties);
    }

    /**
     * Tears down the test environment.
     */
    @After
    public void tearDown() {

        // clears the properties, by restoring the previous properties values
        System.setProperties(systemProperties);
    }

    /**
     * Tests the {@link KayronServerInitializer#initializeProperties(BeforeClass)} method.
     */
    @Test
    public void shouldSetProperties() {

        // given
        assertNull("The property 'archaius.deployment.applicationId' should be null.",
                System.getProperty(APPLICATION_ID_PROPERTY_NAME));
        assertNull("The property 'archaius.deployment.environment' should be null.",
                System.getProperty(ENVIRONMENT_PROPERTY_NAME));

        // when
        instance.initializeProperties(new BeforeClass(TestCase.class));

        // then
        assertEquals("The property 'archaius.deployment.applicationId' hasn't been set.",
                "kayron-test", System.getProperty(APPLICATION_ID_PROPERTY_NAME));
        assertEquals("The property 'archaius.deployment.environment' hasn't been set.",
                "test", System.getProperty(ENVIRONMENT_PROPERTY_NAME));
    }

    /**
     * Tests the {@link KayronServerInitializer#initializeProperties(BeforeClass)} method.
     */
    @Test
    public void shouldSetPropertiesWithDefaultEnvironment() {

        // given
        assertNull("The property 'archaius.deployment.applicationId' should be null.",
                System.getProperty(APPLICATION_ID_PROPERTY_NAME));
        assertNull("The property 'archaius.deployment.environment' should be null.",
                System.getProperty(ENVIRONMENT_PROPERTY_NAME));

        // when
        instance.initializeProperties(new BeforeClass(DevTestCase.class));

        // then
        assertEquals("The property 'archaius.deployment.applicationId' hasn't been set.",
                "kayron-dev", System.getProperty(APPLICATION_ID_PROPERTY_NAME));
        assertEquals("The property 'archaius.deployment.environment' hasn't been set.",
                "dev", System.getProperty(ENVIRONMENT_PROPERTY_NAME));
    }

    /**
     * A simple class that imitates a test case.
     *
     * @author Jakub Narloch (jmnarloch@gmail.com)
     */
    @RunInKaryon(applicationId = "kayron-test", environment = "test")
    private static class TestCase {

        // empty class
    }

    /**
     * A simple class that imitates a test case.
     *
     * @author Jakub Narloch (jmnarloch@gmail.com)
     */
    @RunInKaryon(applicationId = "kayron-dev")
    private static class DevTestCase {

        // empty class
    }
}
