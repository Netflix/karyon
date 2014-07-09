package com.netflix.adminresources;

import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.name.Names;
import com.netflix.explorers.AppConfigGlobalModelContext;
import com.netflix.explorers.ExplorerManager;
import com.netflix.explorers.ExplorersManagerImpl;
import com.netflix.explorers.context.GlobalModelContext;
import com.netflix.explorers.providers.FreemarkerTemplateProvider;
import com.netflix.karyon.health.HealthCheckHandler;
import com.netflix.karyon.health.HealthCheckInvocationStrategy;

import javax.inject.Inject;

class AdminResourcesModule extends AbstractModule {

    private final Provider<HealthCheckInvocationStrategy> strategy;
    private final Provider<HealthCheckHandler> handlerProvider;

    @Inject
    AdminResourcesModule(Provider<HealthCheckInvocationStrategy> strategy, Provider<HealthCheckHandler> handlerProvider) {
        this.strategy = strategy;
        this.handlerProvider = handlerProvider;
    }

    @Override
    protected void configure() {

        bind(HealthCheckInvocationStrategy.class).toProvider(strategy);
        bind(HealthCheckHandler.class).toProvider(handlerProvider);

        bind(String.class).annotatedWith(Names.named("explorerAppName")).toInstance("admin");
        bind(GlobalModelContext.class).to(AppConfigGlobalModelContext.class);
        bind(ExplorerManager.class).to(ExplorersManagerImpl.class);

        bind(AdminResourceExplorer.class);
        bind(FreemarkerTemplateProvider.class);
        bind(AdminResourcesFilter.class).asEagerSingleton();
    }
}
