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
package com.netflix.kayron.server.test.client;

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.test.spi.context.ClassContext;
import org.jboss.arquillian.test.spi.event.suite.AfterSuite;
import org.jboss.arquillian.test.spi.event.suite.BeforeSuite;
import org.jboss.arquillian.test.test.AbstractTestTestBase;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Tests the {@link KayronExtensionArchiveAppender} class.
 *
 * @author Jakub Narloch (jmnarloch@gmail.com)
 */
public class KayronExtensionArchiveAppenderTest extends AbstractTestTestBase {

    /**
     * Represents the instance of the tested class.
     */
    private KayronExtensionArchiveAppender instance;

    /**
     * Represents the arquillian descriptor.
     */
    private ArquillianDescriptor arquillianDescriptor;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addExtensions(List<Class<?>> extensions) {
        extensions.add(KayronExtensionArchiveAppender.class);
    }

    /**
     * Sets up the test environment.
     */
    @Before
    public void setUp() {

        // activates the arquillian context
        getManager().getContext(ClassContext.class).activate(KayronExtensionArchiveAppenderTest.class);

        // given
        instance = new KayronExtensionArchiveAppender();
    }

    /**
     * Tears down the test environment.
     */
    @After
    public void tearDown() {

        // cleans the context
        getManager().fire(new AfterSuite());
        getManager().getContext(ClassContext.class).deactivate();
    }

    /**
     * Tests the {@link KayronExtensionArchiveAppender#buildArchive()} method.
     *
     * @throws IOException
     */
    @Test
    public void shouldBuildArchive() throws IOException {

        // given
        arquillianDescriptor = Descriptors.importAs(ArquillianDescriptor.class)
                .fromFile(new File("src/test/resources", "arquillian.xml"));
        bind(ApplicationScoped.class, ArquillianDescriptor.class, arquillianDescriptor);
        getManager().inject(instance);
        getManager().fire(new BeforeSuite());

        // when
        Archive archive = instance.buildArchive();

        // then
        assertNotNull("The archive hasn't been created.", archive);
        Properties properties = readProperties(archive, "kayron-testsuite.properties");
        assertEquals("The property value is invalid.", "applicationId", properties.getProperty("applicationId"));
        assertEquals("The property value is invalid.", "environment", properties.getProperty("environment"));
    }

    /**
     * Tests the {@link KayronExtensionArchiveAppender#buildArchive()} method.
     *
     * @throws IOException
     */
    @Test
    public void shouldBuildArchiveWithEmptyProperties() throws IOException {

        // given
        arquillianDescriptor = Descriptors.importAs(ArquillianDescriptor.class)
                .fromFile(new File("src/test/resources", "empty-arquillian.xml"));
        bind(ApplicationScoped.class, ArquillianDescriptor.class, arquillianDescriptor);
        getManager().inject(instance);
        getManager().fire(new BeforeSuite());

        // when
        Archive archive = instance.buildArchive();

        // then
        assertNotNull("The archive hasn't been created.", archive);
        Properties properties = readProperties(archive, "kayron-testsuite.properties");
        assertNull("The property value is invalid.", properties.getProperty("applicationId"));
        assertNull("The property value is invalid.", properties.getProperty("environment"));
    }

    /**
     * Reads the properties from the specified {@code path}.
     *
     * @param archive the archive
     * @param path    the path to the properties file
     *
     * @return the loaded properties
     *
     * @throws IOException if any error occurs when loading the properties file
     */
    @SuppressWarnings("PMD.EmptyCatchBlock")
    private Properties readProperties(Archive archive, String path) throws IOException {

        InputStream input = null;
        try {

            StringAsset asset = (StringAsset) archive.get(path).getAsset();
            input = asset.openStream();

            Properties properties = new Properties();
            properties.load(input);
            return properties;
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
}
