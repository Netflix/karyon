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

/**
 * A set of property names that Karyon uses.
 *
 * @author Nitesh Kant
 */
public class PropertyNames {

    /**
     * Common prefix for all karyon properties.
     */
    public static final String KARYON_PROPERTIES_PREFIX = "com.netflix.karyon.";

    /**
     * A fully qualified classname of the application class. Set this in case you have multiple classes annotated with
     * {@link Application} for whatever reason. See {@link Application} for details.
     */
    public static final String EXPLICIT_APPLICATION_CLASS_PROP_NAME = KARYON_PROPERTIES_PREFIX + "app.class";

    /**
     * Set this to true if you want to disable application discovery. See {@link Application} for details.
     */
    public static final String DISABLE_APPLICATION_DISCOVERY_PROP_NAME = KARYON_PROPERTIES_PREFIX + "disable.app.discovery";

    /**
     * Prefix for all eureka related properties. Default is "eureka"
     */
    public static final String EUREKA_PROPERTIES_NAME_PREFIX_PROP_NAME = KARYON_PROPERTIES_PREFIX + "eureka.properties.prefix";

    /**
     * Prefix for all eureka related properties. Default to the value set in {@link PropertyNames#EUREKA_PROPERTIES_NAME_PREFIX_PROP_NAME}
     */
    public static final String EUREKA_CLIENT_PROPERTIES_NAME_PREFIX_PROP_NAME = KARYON_PROPERTIES_PREFIX + "eureka.client.properties.prefix";

    /**
     * Datacenter type for eureka.
     */
    public static final String EUREKA_DATACENTER_TYPE_PROP_NAME = KARYON_PROPERTIES_PREFIX + "eureka.datacenter.type";

    /**
     * Sets this to a comma separated list of fully qualified component class names if you need to explicitly specify
     * component classes.
     */
    public static final String EXPLICIT_COMPONENT_CLASSES_PROP_NAME = KARYON_PROPERTIES_PREFIX + "component.classes";

    /**
     * Prefix for disabling a particular component. See {@link Component} for details.
     */
    public static final String COMPONENT_DISABLE_PROP_PEFIX = KARYON_PROPERTIES_PREFIX + "component.disable.";

    /**
     * Fully qualified classname of custom server bootstrap class.
     */
    public static final String SERVER_BOOTSTRAP_CLASS_OVERRIDE = KARYON_PROPERTIES_PREFIX + "server.bootstrap.class";

    /**
     * Comma separated list of packages used for governator scanning.
     */
    public static final String SERVER_BOOTSTRAP_BASE_PACKAGES_OVERRIDE = KARYON_PROPERTIES_PREFIX + "server.base.packages";

    /**
     * Fully qualified classname for the karyon health check handler.
     */
    public static final String HEALTH_CHECK_HANDLER_CLASS_PROP_NAME = KARYON_PROPERTIES_PREFIX + "health.check.handler.classname";

    public static final String EUREKA_COMPONENT_NAME = "eureka";

    public static final String ARCHAIUS_COMPONENT_NAME = "archaius";

    /**
     * Set this to <code>true</code>  to disable integration with eureka.
     */
    public static final String DISABLE_EUREKA_INTEGRATION = KARYON_PROPERTIES_PREFIX + EUREKA_COMPONENT_NAME + ".disable";

    /**
     * Set this to <code>true</code>  to disable integration with archaius.
     */
    public static final String DISABLE_ARCHAIUS_INTEGRATION = KARYON_PROPERTIES_PREFIX + ARCHAIUS_COMPONENT_NAME + ".disable";

    /**
     * Fully qualified classname of the health check invocation strategy. Default is an async strategy.
     */
    public static final String HEALTH_CHECK_STRATEGY = KARYON_PROPERTIES_PREFIX + "health.check.strategy";

    /**
     * Health check timeout in milliseconds. If the healthcheck does not return in this time, the application is deemed
     * to be unhealthy.
     */
    public static final String HEALTH_CHECK_TIMEOUT_MILLIS = KARYON_PROPERTIES_PREFIX + "health.check.timeout.ms";

    /**
     * Default value of {@link PropertyNames#HEALTH_CHECK_TIMEOUT_MILLIS}
     */
    public static final String HEALTH_CHECK_TIMEOUT_DEFAULT_MILLIS = KARYON_PROPERTIES_PREFIX + "health.check.default.timeout.ms";

    /**
     * If this is set to <code>true</code> the karyon health check results will be sent to eureka by using an adapter
     * to eureka's healthcheck.
     */
    public static final String UNIFY_HEALTHCHECK_WITH_EUREKA = KARYON_PROPERTIES_PREFIX + "unify.health.check.with.eureka";
}
