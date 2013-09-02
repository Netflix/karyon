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

/**
 * Aggregates all of the extension properties.
 *
 * @author Jakub Narloch (jmnarloch@gmail.com)
 */
public class KayronExtensionConfiguration {

    /**
     * Represents the application id.
     */
    private String applicationId;

    /**
     * Represents the kayron environment.
     */
    private String environment;

    /**
     * Creates new instance of {@link KayronExtensionConfiguration} class.
     */
    public KayronExtensionConfiguration() {
        // empty constructor
    }

    /**
     * Retrieves the application id.
     *
     * @return the application id
     */
    public String getApplicationId() {
        return applicationId;
    }

    /**
     * Sets the application id.
     *
     * @param applicationId the application id
     */
    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    /**
     * Retrieves the kayron environment.
     *
     * @return the kayron environment
     */
    public String getEnvironment() {
        return environment;
    }

    /**
     * Sets the kayron environment.
     *
     * @param environment the kayron environment
     */
    public void setEnvironment(String environment) {
        this.environment = environment;
    }
}
