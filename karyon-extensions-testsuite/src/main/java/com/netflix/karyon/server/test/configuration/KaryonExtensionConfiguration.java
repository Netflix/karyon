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
package com.netflix.karyon.server.test.configuration;

/**
 * Aggregates all of the extension properties.
 *
 * @author Jakub Narloch (jmnarloch@gmail.com)
 */
public class KaryonExtensionConfiguration {

    /**
     * Represents the application id.
     */
    private String applicationId;

    /**
     * Represents the karyon environment.
     */
    private String environment;

    /**
     * Whether to package the karyon classes.
     */
    private boolean autoPackage = false;

    /**
     * Creates new instance of {@link KaryonExtensionConfiguration} class.
     */
    public KaryonExtensionConfiguration() {
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
     * Retrieves the karyon environment.
     *
     * @return the karyon environment
     */
    public String getEnvironment() {
        return environment;
    }

    /**
     * Sets the karyon environment.
     *
     * @param environment the karyon environment
     */
    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    /**
     * Retrieves whether to package the karyon packages.
     * <p />
     * Defines whether the extension should also include in the test deployment all the karyon packages, allowing to
     * omit them when creating the test deployment for the particular test case.
     *
     * @return whether to packages the karyon packages
     */
    public boolean isAutoPackage() {
        return autoPackage;
    }

    /**
     * Sets whether to package the karyon packages.
     *
     * @param autoPackage to packages the karyon packages
     *
     * @see {@link #isAutoPackage}
     */
    public void setAutoPackage(boolean autoPackage) {
        this.autoPackage = autoPackage;
    }
}
