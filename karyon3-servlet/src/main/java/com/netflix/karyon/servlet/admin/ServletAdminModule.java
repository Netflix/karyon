package com.netflix.karyon.servlet.admin;

import com.google.inject.AbstractModule;

/**
 * Admin module for exposing servlets and filters configured via ServletModule 
 * 
 * @author elandau
 *
 */
public final class ServletAdminModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(ServletAdminResource.class);
    }
}
