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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * A annotation that can be used to indicate that the given Arquillian test is capable of injecting the Kayron
 * configured components.
 *
 * <p />
 *
 * In order to run such test, the deployment need to properly configure the {@code KayronGuiceContextListener}.
 *
 * @author Jakub Narloch (jmnarloch@gmail.com)
 */
@Documented
@Retention(RUNTIME)
@Target(ElementType.TYPE)
public @interface RunInKaryon {

    /**
     * Specifies the application id that will be used to set up the Kayron environment during the deployment.
     */
    String applicationId() default "";

    /**
     * Allows to define the Kayron environment.
     *
     * <p />
     *
     * Defaults to {@code dev}.
     */
    String environment() default "";
}
