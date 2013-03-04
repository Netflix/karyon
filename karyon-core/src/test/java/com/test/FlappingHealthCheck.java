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

package com.test;

import com.netflix.karyon.spi.HealthCheckHandler;

import javax.annotation.PostConstruct;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Nitesh Kant
 */
public class FlappingHealthCheck implements HealthCheckHandler {

    private AtomicBoolean returnSuccess = new AtomicBoolean(true);

    @PostConstruct
    public void init() {
        RegistrationSequence.addClass(this.getClass());
    }

    @Override
    public int getStatus() {
        if (returnSuccess.compareAndSet(true, false)) {
            return 200;
        } else {
            return 500;
        }
    }
}
