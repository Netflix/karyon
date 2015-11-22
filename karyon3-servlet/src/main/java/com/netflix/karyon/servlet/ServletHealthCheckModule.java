package com.netflix.karyon.servlet;

import com.google.inject.AbstractModule;
import com.google.inject.servlet.ServletModule;

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
