package com.netflix.karyon.servlet;

import com.google.inject.AbstractModule;
import com.netflix.karyon.admin.AdminModule;
import com.netflix.karyon.conditional.ConditionalOnModule;

/**
 * Admin module for exposing servlets and filters configured via ServletModule 
 * 
 * @author elandau
 *
 */
@ConditionalOnModule({AdminModule.class})
public final class ServletAdminModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(ServletAdminResource.class);
    }
}
