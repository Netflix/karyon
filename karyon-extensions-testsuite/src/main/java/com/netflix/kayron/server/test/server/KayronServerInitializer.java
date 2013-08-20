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
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.event.suite.BeforeClass;

/**
 * A test initializer that sets up the Kayron environment in the server.
 *
 * @author Jakub Narloch (jmnarloch@gmail.com)
 */
public class KayronServerInitializer {

    /**
     * Handles the before test event.
     *
     * @param event the before test event
     */
    public void initializeProperties(@Observes BeforeClass event) {

        RunInKaryon annotation = event.getTestClass().getAnnotation(RunInKaryon.class);

        if (annotation != null) {

            if (!annotation.applicationId().isEmpty()) {
                System.setProperty("archaius.deployment.applicationId", annotation.applicationId());
            }

            if (!annotation.environment().isEmpty()) {
                System.setProperty("archaius.deployment.environment", annotation.environment());
            }
        }
    }
}
