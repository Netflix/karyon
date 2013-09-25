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

import com.netflix.karyon.server.test.configuration.KaryonExtensionConfiguration;
import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.test.spi.context.ClassContext;
import org.jboss.arquillian.test.spi.event.suite.AfterSuite;
import org.jboss.arquillian.test.spi.event.suite.BeforeSuite;
import org.jboss.arquillian.test.test.AbstractTestTestBase;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link KaryonExtensionConfigrationProducer} class.
 *
 * @author Jakub Narloch (jmnarloch@gmail.com)
 */
public class KaryonExtensionConfigrationProducerTest extends AbstractTestTestBase {

    private KaryonExtensionConfigrationProducer instance;

    private ArquillianDescriptor arquillianDescriptor;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addExtensions(List<Class<?>> extensions) {
        extensions.add(KaryonExtensionConfigrationProducer.class);
    }

    @Before
    public void setUp() {

        // activates the arquillian context
        getManager().getContext(ClassContext.class).activate(KaryonExtensionConfigrationProducerTest.class);

        // given
        instance = new KaryonExtensionConfigrationProducer();
    }

    @After
    public void tearDown() {

        // cleans the context
        getManager().fire(new AfterSuite());
        getManager().getContext(ClassContext.class).deactivate();
    }

    /**
     * Tests the {@link KaryonExtensionConfigrationProducer#loadExtensionConfiguration(BeforeSuite)} method.
     */
    @Test
    public void shouldLoadProperties() {

        // when
        arquillianDescriptor = Descriptors.importAs(ArquillianDescriptor.class)
                .fromFile(new File("src/test/resources", "arquillian.xml"));
        bind(ApplicationScoped.class, ArquillianDescriptor.class, arquillianDescriptor);
        getManager().fire(new BeforeSuite());

        // then
        KaryonExtensionConfiguration config = getManager().resolve(KaryonExtensionConfiguration.class);

        assertNotNull("The configuration hasn't been created.", config);
        assertEquals("The configuration has invalid property.", "applicationId", config.getApplicationId());
        assertEquals("The configuration has invalid property.", "environment", config.getEnvironment());
        assertTrue("The configuration has invalid property value.", config.isAutoPackage());
    }

    /**
     * Tests the {@link KaryonExtensionConfigrationProducer#loadExtensionConfiguration(BeforeSuite)} method.
     */
    @Test
    public void shouldLoadPropertiesWithDefaults() {

        // when
        arquillianDescriptor = Descriptors.importAs(ArquillianDescriptor.class)
                .fromFile(new File("src/test/resources", "empty-arquillian.xml"));
        bind(ApplicationScoped.class, ArquillianDescriptor.class, arquillianDescriptor);
        getManager().fire(new BeforeSuite());

        // then
        KaryonExtensionConfiguration config = getManager().resolve(KaryonExtensionConfiguration.class);

        assertNotNull("The configuration hasn't been created.", config);
        assertNull("The configuration has invalid property value.", config.getApplicationId());
        assertNull("The configuration has invalid property value.", config.getEnvironment());
        assertFalse("The configuration has invalid property value.", config.isAutoPackage());
    }
}
