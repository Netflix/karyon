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

package com.netflix.karyon.spi;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation specifying that the containing class is an application which holds the basic functioning of the module. <p/>
 * An application is a special type of {@link com.netflix.governator.annotations.AutoBindSingleton} which requires a
 * set of {@link Component} to be initialized before it.  <br/>
 * It is not mandatory to have an application class explicitly annotated with this annotation if there is no dependency
 * on any components or there is a direct dependency on a component i.e. via {@link com.google.inject.Inject} or
 * {@link com.netflix.governator.annotations.AutoBind}. In such a case, you can just mark your class as a
 * {@link com.netflix.governator.annotations.AutoBindSingleton} <br/>
 *
 * All governated classes will be instantiated when karyon initializes. <br/>
 *
 * <h3>Controlling the application discovery</h3>
 *
 * The automatic discovery of application can be disabled by setting a property
 * {@link PropertyNames#DISABLE_APPLICATION_DISCOVERY_PROP_NAME} available to archaius to <code>true</code>  <br/>
 * If there are multiple classes annotated with {@link Application} for whatever reasons, one can choose to explicitly
 * tell karyon about the application class by specifying the fully qualified classname as a property with name
 * {@link PropertyNames#EXPLICIT_APPLICATION_CLASS_PROP_NAME}
 *
 * @author Nitesh Kant
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Application {

}
