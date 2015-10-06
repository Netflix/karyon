package com.netflix.karyon.eureka;

import com.netflix.discovery.guice.EurekaModule;
import com.netflix.karyon.admin.AbstractAdminModule;
import com.netflix.karyon.admin.AdminModule;
import com.netflix.karyon.conditional.ConditionalOnModule;

@ConditionalOnModule({AdminModule.class, EurekaModule.class})
public final class EurekaAdminModule extends AbstractAdminModule {
    @Override
    protected void configure() {
        bindAdminResource("eureka-apps").to(EurekaApplicationAdminResource.class);
        bindAdminResource("eureka-status").to(EurekaStatusAdminResource.class);
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
