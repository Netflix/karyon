package com.google.inject.servlet;

import com.google.inject.AbstractModule;
import com.netflix.karyon.conditional.ConditionalOnModule;

@ConditionalOnModule(InternalServletModule.class)
public class ServletHealthCheckModule extends AbstractModule {
    @Override
    protected void configure() {
        install(new ServletModule() {
            @Override
            protected void configureServlets() {
                serve("/health").with(HealthCheckServlet.class);
                bind(HealthCheckServlet.class);
            }
        });
    }
}
