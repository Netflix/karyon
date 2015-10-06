package com.netflix.karyon.servlet;

import com.netflix.karyon.admin.AbstractAdminModule;
import com.netflix.karyon.admin.AdminModule;
import com.netflix.karyon.conditional.ConditionalOnModule;

/**
 * Admin module for exposing servlets and filters configured via ServletModule 
 * 
 * @author elandau
 *
 */
@ConditionalOnModule({AdminModule.class})
public final class ServletAdminModule extends AbstractAdminModule {
    @Override
    protected void configure() {
        bindAdminResource("servlet").to(ServletAdminResource.class);
    }
}
