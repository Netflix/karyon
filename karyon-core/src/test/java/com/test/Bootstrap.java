package com.test;

import com.google.inject.Binder;
import com.google.inject.Injector;
import com.netflix.governator.configuration.ConfigurationProvider;
import com.netflix.governator.guice.BootstrapBinder;
import com.netflix.governator.guice.BootstrapModule;
import com.netflix.governator.guice.LifecycleInjectorBuilder;
import com.netflix.karyon.server.ServerBootstrap;
import com.netflix.karyon.spi.ServiceRegistryClient;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Nitesh Kant
 */
public class Bootstrap extends ServerBootstrap {

    public static final String NEW_LIFECYCLE_INJ_BUILDER = "newLifecycleInjectorBuilder";
    public static final String GET_CONFIG_PROVIDER = "getConfigurationProvider";
    public static final String GET_SERVICE_REG_CLIENT = "getServiceRegistryClient";
    public static final String BEFORE_INJECTION_CREATION = "beforeInjectorCreation";
    public static final String CREATE_INJECTOR = "createInjector";
    public static final String CONFIGURE_BINDER = "configureBinder";
    public static final String CONFIGURE_BOOT_BINDER = "configureBootstrapBinder";
    public static final String GET_BOOT_MODULE = "getBootstrapModule";
    public static final String GET_BASE_PKGS = "getBasePackages";

    private final Set<String> callbacksInvoked = new HashSet<String>();

    @Override
    protected LifecycleInjectorBuilder newLifecycleInjectorBuilder() {
        callbacksInvoked.add(NEW_LIFECYCLE_INJ_BUILDER);
        return super.newLifecycleInjectorBuilder();
    }

    @Nullable
    @Override
    protected Class<? extends ConfigurationProvider> getConfigurationProvider() {
        callbacksInvoked.add(GET_CONFIG_PROVIDER);
        return super.getConfigurationProvider();
    }

    @Nullable
    @Override
    protected Class<? extends ServiceRegistryClient> getServiceRegistryClient() {
        callbacksInvoked.add(GET_SERVICE_REG_CLIENT);
        return super.getServiceRegistryClient();
    }

    @Override
    protected void beforeInjectorCreation(@SuppressWarnings("unused") LifecycleInjectorBuilder builderToBeUsed) {
        callbacksInvoked.add(BEFORE_INJECTION_CREATION);
        super.beforeInjectorCreation(builderToBeUsed);
    }

    @Override
    protected Injector createInjector(LifecycleInjectorBuilder builder) {
        callbacksInvoked.add(CREATE_INJECTOR);
        return super.createInjector(builder);
    }

    @Override
    protected void configureBinder(@SuppressWarnings("unused") Binder binder) {
        callbacksInvoked.add(CONFIGURE_BINDER);
        super.configureBinder(binder);
    }

    @Override
    protected void configureBootstrapBinder(@SuppressWarnings("unused") BootstrapBinder bootstrapBinder) {
        callbacksInvoked.add(CONFIGURE_BOOT_BINDER);
        super.configureBootstrapBinder(bootstrapBinder);
    }

    @Override
    protected BootstrapModule getBootstrapModule() {
        callbacksInvoked.add(GET_BOOT_MODULE);
        return super.getBootstrapModule();
    }

    @Nullable
    @Override
    protected Collection<String> getBasePackages() {
        callbacksInvoked.add(GET_BASE_PKGS);
        return super.getBasePackages();
    }

    public Set<String> getCallbacksInvoked() {
        return callbacksInvoked;
    }
}
