package com.netflix.karyon;

import com.netflix.governator.guice.annotations.Bootstrap;
import com.netflix.karyon.health.AlwaysHealthyHealthCheck;
import com.netflix.karyon.health.HealthCheckHandler;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Nitesh Kant
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Bootstrap(KaryonBootstrapSuite.class)
public @interface KaryonBootstrap {

    String name();

    Class<? extends HealthCheckHandler> healthcheck() default AlwaysHealthyHealthCheck.class;
}
