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
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.event.suite.BeforeClass;

/**
 * A test initializer that sets up the Kayron environment in the server.
 *
 * @author Jakub Narloch (jmnarloch@gmail.com)
 */
public class KayronServerInitializer {

    /**
     * Represents the default application id.
     */
    private static final String DEFAULT_APPLICATION_ID = "";

    /**
     * Represents the default environment value.
     */
    private static final String DEFAULT_ENVIRONMENT = "dev";

    /**
     * The extension configuration.
     */
    @Inject
    private Instance<KayronExtensionConfiguration> configuration;

    /**
     * Handles the before test event.
     *
     * @param event the before test event
     */
    public void initializeProperties(@Observes BeforeClass event) {

        String applicationId;
        String environment;

        RunInKaryon annotation = event.getTestClass().getAnnotation(RunInKaryon.class);

        if (annotation != null) {

            applicationId = getValue(annotation.applicationId(), configuration.get().getApplicationId(),
                    DEFAULT_APPLICATION_ID);
            environment = getValue(annotation.environment(), configuration.get().getEnvironment(), DEFAULT_ENVIRONMENT);

            if (!applicationId.isEmpty()) {
                System.setProperty("archaius.deployment.applicationId", applicationId);
            }

            if (!environment.isEmpty()) {
                System.setProperty("archaius.deployment.environment", environment);
            }
        }
    }

    /**
     * Retrieves the value of the property, taking the annotation value with higher precedence over the configuration.
     * If neither the annotation or configuration specifies the property, the default value is being returned.
     *
     * @param annotationValue the annotation value
     * @param configValue     the config value
     * @param defaultValue    the default value
     *
     * @return the property value
     */
    private String getValue(String annotationValue, String configValue, String defaultValue) {

        String result = getValue(annotationValue, configValue);

        return getValue(result, defaultValue);
    }

    /**
     * Returns the passed {@code value} if it's not null and non empty, in order case the {@code defaultValue} is being
     * returned.
     *
     * @param value        the value
     * @param defaultValue the default value
     *
     * @return property value
     */
    private String getValue(String value, String defaultValue) {

        return value != null && !value.isEmpty() ? value : defaultValue;
    }
}
