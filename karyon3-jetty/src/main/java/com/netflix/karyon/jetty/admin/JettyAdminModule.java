package com.netflix.karyon.jetty.admin;

import com.google.inject.AbstractModule;

/**
 * Admin module for exposing Jetty's configuration information
 * 
 * @author elandau
 *
 */
public final class JettyAdminModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(JettyAdminResource.class);
    }
}
