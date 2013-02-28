package com.netflix.karyon.spi;

/**
 * A set of property names that Karyon uses.
 *
 * @author Nitesh Kant
 */
public class PropertyNames {

    public static final String KARYON_PROPERTIES_PREFIX = "com.netflix.karyon.";

    public static final String EXPLICIT_APPLICATION_CLASS_PROP_NAME = KARYON_PROPERTIES_PREFIX + "app.class";

    public static final String DISABLE_APPLICATION_DISCOVERY_PROP_NAME = KARYON_PROPERTIES_PREFIX + "disable.app.discovery";

    public static final String EUREKA_PROPERTIES_NAME_PREFIX_PROP_NAME = KARYON_PROPERTIES_PREFIX + "eureka.properties.prefix";

    public static final String EUREKA_DATACENTER_TYPE_PROP_NAME = KARYON_PROPERTIES_PREFIX + "eureka.datacenter.type";

    public static final String EXPLICIT_COMPONENT_CLASSES_PROP_NAME = KARYON_PROPERTIES_PREFIX + "component.classes";

    public static final String COMPONENT_DISABLE_PROP_PEFIX = KARYON_PROPERTIES_PREFIX + "component.disable.";

    public static final String SERVER_BOOTSTRAP_CLASS_OVERRIDE = KARYON_PROPERTIES_PREFIX + "server.bootstrap.class";

    public static final String SERVER_BOOTSTRAP_BASE_PACKAGES_OVERRIDE = KARYON_PROPERTIES_PREFIX + "server.base.packages";

    public static final String HEALTH_CHECK_HANDLER_CLASS_PROP_NAME = KARYON_PROPERTIES_PREFIX + "health.check.handler.classname";

    public static final String EUREKA_COMPONENT_NAME = "eureka";

    public static final String DISABLE_EUREKA_INTEGRATION = KARYON_PROPERTIES_PREFIX + EUREKA_COMPONENT_NAME + ".disable";

    public static final String HEALTH_CHECK_STRATEGY = KARYON_PROPERTIES_PREFIX + "health.check.strategy";

    public static final String HEALTH_CHECK_TIMEOUT_MILLIS = KARYON_PROPERTIES_PREFIX + "health.check.timeout.ms";

    public static final String HEALTH_CHECK_TIMEOUT_DEFAULT_MILLIS = KARYON_PROPERTIES_PREFIX + "health.check.default.timeout.ms";

    public static final String UNIFY_HEALTHCHECK_WITH_EUREKA = KARYON_PROPERTIES_PREFIX + "unify.health.check.with.eureka";
}
