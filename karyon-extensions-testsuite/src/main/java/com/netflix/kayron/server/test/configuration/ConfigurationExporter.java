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
 * Exports the configuration object. The result of this method will be used for exporting the assets with the test
 * deployment.
 *
 * @author Jakub Narloch (jmnarloch@gmail.com)
 */
public interface ConfigurationExporter {

    /**
     * Exports the configuration as a string representation.
     *
     * @return the spring representtion of the properties
     */
    String exportAsString();
}
