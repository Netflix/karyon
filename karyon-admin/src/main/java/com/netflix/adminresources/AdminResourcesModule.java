package com.netflix.adminresources;

import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.name.Names;
import com.netflix.explorers.AppConfigGlobalModelContext;
import com.netflix.explorers.ExplorerManager;
import com.netflix.explorers.ExplorersManagerImpl;
import com.netflix.explorers.context.GlobalModelContext;
import com.netflix.explorers.providers.FreemarkerTemplateProvider;
import com.netflix.karyon.server.HealthCheckModule;
import com.netflix.karyon.server.eureka.HealthCheckInvocationStrategy;

class AdminResourcesModule
        extends AbstractModule {
    private final Provider<HealthCheckInvocationStrategy> healthCheckInvocationStrategyProvider;

    public AdminResourcesModule(Provider<HealthCheckInvocationStrategy> healthCheckInvocationStrategyProvider) {
        this.healthCheckInvocationStrategyProvider = healthCheckInvocationStrategyProvider;
    }

    @Override
    protected void configure() {
        bind(String.class).annotatedWith(Names.named("explorerAppName")).toInstance("admin");
        bind(GlobalModelContext.class).to(AppConfigGlobalModelContext.class);
        bind(ExplorerManager.class).to(ExplorersManagerImpl.class);

        bind(AdminResourceExplorer.class);
        bind(FreemarkerTemplateProvider.class);
        bind(AdminResourcesFilter.class).asEagerSingleton();

        if (healthCheckInvocationStrategyProvider != null) {
            bind(HealthCheckInvocationStrategy.class).toProvider(healthCheckInvocationStrategyProvider);
        } else {
            install(new HealthCheckModule());
        }

    }
}
