package com.netflix.karyon.server.governator;

import javax.servlet.ServletContextListener;

import com.google.inject.AbstractModule;
import com.google.inject.binder.AnnotatedBindingBuilder;
import com.google.inject.binder.LinkedBindingBuilder;
import com.netflix.karyon.server.eureka.HealthCheckInvocationStrategy;
import com.netflix.karyon.spi.HealthCheckHandler;

/**
 * Convenience module with simplified bindXXX methods for various Karyon bindings
 * @author elandau
 *
 */
public class KaryonModule extends AbstractModule {
    
    protected AnnotatedBindingBuilder<ServletContextListener> bindServletContextListener() {
        return bind(ServletContextListener.class);
    }
    
    protected AnnotatedBindingBuilder<HealthCheckHandler> bindHealthCheckHandler() {
        return bind(HealthCheckHandler.class);
    }

    protected LinkedBindingBuilder<HealthCheckInvocationStrategy> bindHealthCheckInvocationStrategy() {
        return bind(HealthCheckInvocationStrategy.class);
    }

    @Override
    protected final void configure() {
        configureKaryon();
    }
    
    protected void configureKaryon() {}
}
