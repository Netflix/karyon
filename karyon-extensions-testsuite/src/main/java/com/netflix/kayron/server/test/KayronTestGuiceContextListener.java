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
package com.netflix.kayron.server.test;

import com.google.inject.Injector;
import com.netflix.karyon.server.guice.KaryonGuiceContextListener;

/**
 * A subclass of {@link KaryonGuiceContextListener} that simply captures the injector instance and stores it in static
 * field.
 *
 * @author Jakub Narloch (jmnarloch@gmail.com)
 */
public class KayronTestGuiceContextListener extends KaryonGuiceContextListener {

    /**
     * The instance of the injector used by the Kayron.
     */
    private static Injector injector;

    /**
     * Retrieves the servlet context injector.
     *
     * @return the servlet context injector
     */
    public static synchronized Injector getServletContextInjector() {
        return KayronTestGuiceContextListener.injector;
    }

    /**
     * Sets the servlet context injector.
     *
     * @param injector the servlet context injector
     */
    public static synchronized void setServletContextInjector(Injector injector) {
        KayronTestGuiceContextListener.injector = injector;
    }

    /**
     * Overrides the default implementation and additionally 'captures' the created injector instance.
     *
     * @return the injector
     */
    @Override
    protected Injector getInjector() {
        Injector injector = super.getInjector();
        setServletContextInjector(injector);
        return injector;
    }
}
