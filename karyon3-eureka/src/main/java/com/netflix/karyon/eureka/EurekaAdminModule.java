package com.netflix.karyon.eureka;

import com.google.inject.AbstractModule;
import com.netflix.discovery.guice.EurekaModule;
import com.netflix.karyon.admin.AdminModule;
import com.netflix.karyon.conditional.ConditionalOnModule;

@ConditionalOnModule({AdminModule.class, EurekaModule.class})
public final class EurekaAdminModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(EurekaApplicationAdminResource.class);
        bind(EurekaStatusAdminResource.class);
    }
}
