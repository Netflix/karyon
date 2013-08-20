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

import com.google.inject.Injector;
import com.netflix.kayron.server.test.KayronTestGuiceContextListener;
import com.netflix.kayron.server.test.RunInKaryon;
import org.jboss.arquillian.test.spi.TestEnricher;

import java.lang.reflect.Method;

/**
 * The Arquillian test enricher, that is able to inject the Kayron configured components.
 * <p />
 * Basically this enricher injects all the Guice configured injection points.
 *
 * @author Jakub Narloch (jmnarloch@gmail.com)
 */
public class KayronTestEnricher implements TestEnricher {

    /**
     * Enriches the test instance, by injecting all Kayron configured dependencies.
     *
     * @param testCase the test instance
     */
    @Override
    public void enrich(Object testCase) {

        // verifies whether the test case should be enriched
        if (testCase.getClass().isAnnotationPresent(RunInKaryon.class)) {

            // tries to enquire the injector
            Injector injector = getServletContextInjector();

            if (injector == null) {

                throw new RuntimeException("The Injector instance could not be enquired."
                        + " Have you configured the KaryonGuiceContextListener properly?");
            }

            injector.injectMembers(testCase);
        }
    }

    /**
     * Resolves the test method parameters.
     * <p />
     * The implementation does nothing, because the underlying Guice injector is not capable of injecting the method
     * parameters.
     *
     * @param method the test method
     *
     * @return an empty array
     */
    @Override
    public Object[] resolve(Method method) {
        return new Object[0];
    }

    /**
     * Retrieves the Kayron configured injector from the servlet context.
     *
     * @return the injector
     */
    private Injector getServletContextInjector() {

        return KayronTestGuiceContextListener.getServletContextInjector();
    }
}
