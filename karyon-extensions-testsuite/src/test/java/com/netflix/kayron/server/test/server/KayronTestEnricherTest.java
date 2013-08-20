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

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.netflix.kayron.server.test.KayronTestGuiceContextListener;
import com.netflix.kayron.server.test.RunInKaryon;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Tests the {@link KayronTestEnricher} class.
 *
 * @author Jakub Narloch (jmnarloch@gmail.com)
 */
public class KayronTestEnricherTest {

    /**
     * Represents the instance of the tested class.
     */
    private KayronTestEnricher instance;

    /**
     * Sets up the model environment.
     */
    @Before
    public void setUp() {

        // given
        instance = new KayronTestEnricher();

        // creates injector instance
        Injector injector = Guice.createInjector(new AbstractModule() {

            @Override
            protected void configure() {
                // empty
            }
        });
        KayronTestGuiceContextListener.setServletContextInjector(injector);
    }

    /**
     * Test the {@link KayronTestEnricher#enrich(Object)} method.
     */
    @Test
    public void shouldEnrichInstance() {

        // given
        TestCase testCase = new TestCase();
        assertNull("The dependency can not be null.", testCase.getComponent());

        // when
        instance.enrich(testCase);

        // then
        assertNotNull("The test hasn't been enriched.", testCase.getComponent());
    }

    /**
     * Test the {@link KayronTestEnricher#enrich(Object)} method on a test instance that is not annotated with
     * {@link RunInKaryon}.
     */
    @Test
    public void shouldNotEnrichInstance() {

        // given
        PlainTestCase testCase = new PlainTestCase();
        assertNull("The dependency can not be null.", testCase.getComponent());

        // when
        instance.enrich(testCase);

        // then
        assertNull("The component shouldn't be injected.", testCase.getComponent());
    }

    /**
     * Test the {@link KayronTestEnricher#enrich(Object)} method when there is no captured injector.
     */
    @Test(expected = RuntimeException.class)
    public void shouldThrowException() {

        // given
        KayronTestGuiceContextListener.setServletContextInjector(null);
        TestCase testCase = new TestCase();
        assertNull("The dependency can not be null.", testCase.getComponent());

        // when
        instance.enrich(testCase);
    }

    /**
     * Imitates a simple test case. This class isn't annotated with {@link RunInKaryon} so it won't it's
     * dependencies won't be injected.
     *
     * @author Jakub Narloch (jmnarloch@gmail.com)
     */
    private static class PlainTestCase {

        @Inject
        private Component component;

        private Component getComponent() {
            return component;
        }
    }

    /**
     * Imitates a simple test case.
     *
     * @author Jakub Narloch (jmnarloch@gmail.com)
     */
    @RunInKaryon
    private static class TestCase {

        @Inject
        private Component component;

        private Component getComponent() {
            return component;
        }
    }

    /**
     * Imitates a simple test case dependency.
     *
     * @author Jakub Narloch (jmnarloch@gmail.com)
     */
    private static class Component {

        // empty class
    }
}
