package com.netflix.karyon;

import com.netflix.governator.guice.LifecycleInjectorBuilderSuite;
import com.netflix.governator.guice.annotations.Bootstrap;
import com.netflix.karyon.health.AlwaysHealthyHealthCheck;
import com.netflix.karyon.health.HealthCheckHandler;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * @author Nitesh Kant
 */
@Documented
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Bootstrap(KaryonBootstrapSuite.class)
public @interface KaryonBootstrap {
    /**
     * THE name of the application.  This name is used throughout for things like configuration
     * loading.
     */
    String name();

    /**
     * Provide a custom implementation for HealthCheckHandler.  
     */
    Class<? extends HealthCheckHandler> healthcheck() default AlwaysHealthyHealthCheck.class;

    /**
     * Provide additional LifecycleInjectorBuilderSuite's that will be configuration.  Use
     * this for suites that don't have a Bootstrap annotation.
     */
    Class<? extends LifecycleInjectorBuilderSuite>[] suites() default {};
}
