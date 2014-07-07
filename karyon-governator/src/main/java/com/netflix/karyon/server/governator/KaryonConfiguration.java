package com.netflix.karyon.server.governator;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.servlet.ServletContextListener;

import com.netflix.governator.guice.annotations.Bootstrap;
import com.netflix.karyon.server.eureka.AsyncHealthCheckInvocationStrategy;
import com.netflix.karyon.server.eureka.HealthCheckInvocationStrategy;
import com.netflix.karyon.spi.DefaultHealthCheckHandler;
import com.netflix.karyon.spi.HealthCheckHandler;

/**
 * Governator Bootstrap annotation for Karyon.
 * 
 * @author elandau
 */
@Documented
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Bootstrap(KaryonBootstrap.class)
public @interface KaryonConfiguration {
    Class<? extends ServletContextListener> listener() default DefaultServletContextListener.class;

    Class<? extends HealthCheckHandler> healthcheck() default DefaultHealthCheckHandler.class;

    Class<? extends HealthCheckInvocationStrategy> healthcheckStrategy() default AsyncHealthCheckInvocationStrategy.class;
}
