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

package com.netflix.hellonoss.core;

import com.google.inject.Singleton;
import com.netflix.karyon.spi.Component;

import javax.annotation.PostConstruct;

/**
 * @author Nitesh Kant (nkant@netflix.com)
 */
@Component
@Singleton
public class HelloworldComponent {

    private String helloString = "I am a component";
    private boolean isReady;

    @PostConstruct
    public void initialize() {
        // TODO: Initialization logic, eg: connection to DB etc.
    }

    public String getHelloString() {
        return helloString;
    }

    public boolean isReady() { // To simulate realworld healthcheck, by checking if the component is ready to serve traffic.
        return isReady;
    }

    public void setReady(boolean isReady) { // Providing a hook for externally driven availability. (testing purpose)
        this.isReady = isReady;
    }
}
