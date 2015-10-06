package com.netflix.karyon.jetty;

import com.netflix.governator.guice.jetty.JettyModule;
import com.netflix.karyon.admin.AbstractAdminModule;
import com.netflix.karyon.admin.AdminModule;
import com.netflix.karyon.conditional.ConditionalOnModule;

/**
 * Admin module for exposing Jetty's configuration information
 * 
 * @author elandau
 *
 */
@ConditionalOnModule({AdminModule.class, JettyModule.class})
public final class JettyAdminModule extends AbstractAdminModule {
    @Override
    protected void configure() {
        bindAdminResource("jetty").to(JettyAdminResource.class);
    }
    
    @Override
    public boolean equals(Object obj) {
        return getClass().equals(obj.getClass());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
