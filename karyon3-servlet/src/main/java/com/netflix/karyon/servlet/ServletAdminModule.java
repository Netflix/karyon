package com.netflix.karyon.servlet;

import com.netflix.governator.auto.annotations.ConditionalOnModule;
import com.netflix.karyon.admin.AbstractAdminModule;
import com.netflix.karyon.admin.AdminModule;

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
