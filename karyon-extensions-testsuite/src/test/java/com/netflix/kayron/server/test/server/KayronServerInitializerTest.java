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
import com.netflix.kayron.server.test.configuration.KayronExtensionConfiguration;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.test.spi.context.ClassContext;
import org.jboss.arquillian.test.spi.event.suite.AfterSuite;
import org.jboss.arquillian.test.spi.event.suite.BeforeClass;
import org.jboss.arquillian.test.spi.event.suite.BeforeSuite;
import org.jboss.arquillian.test.test.AbstractTestTestBase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Tests the {@link KayronServerInitializer} class.
 *
 * @author Jakub Narloch (jmnarloch@gmail.com)
 */
public class KayronServerInitializerTest extends AbstractTestTestBase {

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
     * The configuration instance.
     */
    private KayronExtensionConfiguration configuration;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addExtensions(List<Class<?>> extensions) {
        extensions.add(KayronServerInitializer.class);
    }

    /**
     * Sets up the test environment.
     */
    @Before
    public void setUp() {

        // activates the arquillian context
        getManager().getContext(ClassContext.class).activate(KayronServerInitializerTest.class);
        getManager().fire(new BeforeSuite());

        // given
        configuration = new KayronExtensionConfiguration();
        bind(ApplicationScoped.class, KayronExtensionConfiguration.class, configuration);

        instance = new KayronServerInitializer();
        getManager().inject(instance);

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

        // cleans the context
        getManager().fire(new AfterSuite());
        getManager().getContext(ClassContext.class).deactivate();
    }

    /**
     * Tests the {@link KayronServerInitializer#initializeProperties(BeforeClass)} method.
     */
    @Test
    public void shouldSetPropertiesFromAnnotation() {

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
    public void shouldSetPropertiesFromAnnotationWithDefaultEnvironment() {

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
     * Tests the {@link KayronServerInitializer#initializeProperties(BeforeClass)} method.
     */
    @Test
    public void shouldSetPropertiesFromConfiguration() {

        // given
        assertNull("The property 'archaius.deployment.applicationId' should be null.",
                System.getProperty(APPLICATION_ID_PROPERTY_NAME));
        assertNull("The property 'archaius.deployment.environment' should be null.",
                System.getProperty(ENVIRONMENT_PROPERTY_NAME));

        configuration.setApplicationId("kayron-config");
        configuration.setEnvironment("test");

        // when
        instance.initializeProperties(new BeforeClass(ConfigurationTestCase.class));

        // then
        assertEquals("The property 'archaius.deployment.applicationId' hasn't been set.",
                "kayron-config", System.getProperty(APPLICATION_ID_PROPERTY_NAME));
        assertEquals("The property 'archaius.deployment.environment' hasn't been set.",
                "test", System.getProperty(ENVIRONMENT_PROPERTY_NAME));
    }

    /**
     * Tests the {@link KayronServerInitializer#initializeProperties(BeforeClass)} method.
     */
    @Test
    public void shouldSetPropertiesFromConfigurationWithDefaultEnvironment() {

        // given
        assertNull("The property 'archaius.deployment.applicationId' should be null.",
                System.getProperty(APPLICATION_ID_PROPERTY_NAME));
        assertNull("The property 'archaius.deployment.environment' should be null.",
                System.getProperty(ENVIRONMENT_PROPERTY_NAME));

        configuration.setApplicationId("kayron-config");

        // when
        instance.initializeProperties(new BeforeClass(ConfigurationTestCase.class));

        // then
        assertEquals("The property 'archaius.deployment.applicationId' hasn't been set.",
                "kayron-config", System.getProperty(APPLICATION_ID_PROPERTY_NAME));
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

    /**
     * A simple class that imitates a test case.
     *
     * @author Jakub Narloch (jmnarloch@gmail.com)
     */
    @RunInKaryon
    private static class ConfigurationTestCase {

        // empty class
    }
}
