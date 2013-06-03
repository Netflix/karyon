/*
 * Copyright 2013 Netflix, Inc.
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */

package com.netflix.karyon.server;

import com.google.common.collect.Lists;
import com.google.inject.Binder;
import com.netflix.config.ConfigurationManager;
import com.netflix.governator.configuration.ArchaiusConfigurationProvider;
import com.netflix.governator.configuration.ConfigurationProvider;
import com.netflix.governator.guice.BootstrapBinder;
import com.netflix.governator.guice.BootstrapModule;
import com.netflix.governator.guice.LifecycleInjector;
import com.netflix.governator.guice.LifecycleInjectorBuilder;
import com.netflix.governator.lifecycle.ClasspathScanner;
import com.netflix.karyon.server.eureka.AsyncHealthCheckInvocationStrategy;
import com.netflix.karyon.server.eureka.EurekaHandler;
import com.netflix.karyon.server.eureka.HealthCheckInvocationStrategy;
import com.netflix.karyon.server.utils.KaryonUtils;
import com.netflix.karyon.spi.Application;
import com.netflix.karyon.spi.Component;
import com.netflix.karyon.spi.HealthCheckHandler;
import com.netflix.karyon.spi.PropertyNames;
import com.netflix.karyon.spi.ServiceRegistryClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class is the point where the karyon environment in bootstrapped which more or less is the bootstrapping of
 * <a href="https://github.com/Netflix/governator/">Governator</a>. <br/>
 *
 * <h4>{@link LifecycleInjector}</h4>
 *
 * This class creates a {@link LifecycleInjector} for governator to be used by {@link KaryonServer} to create a guice
 * injector. The injector is created with the following components:
 * <ul>
 <li>Configures base packages for governator classpath scanning as "com.netflix" and any extra packages as specified
 by a property {@link PropertyNames#SERVER_BOOTSTRAP_BASE_PACKAGES_OVERRIDE} as a comman separated list of packages to
 scan by governator.</li>
 <li>Binds the {@link ArchaiusConfigurationProvider} as governator's {@link com.netflix.governator.configuration.ConfigurationProvider}</li>
 <li>Binds appropriate {@link HealthCheckHandler} implementation as specified by
 {@link PropertyNames#HEALTH_CHECK_HANDLER_CLASS_PROP_NAME} or a default handler {@link com.netflix.karyon.spi.DefaultHealthCheckHandler}</li>
 <li>Binds appropriate {@link HealthCheckInvocationStrategy} as specified by
 {@link PropertyNames#HEALTH_CHECK_STRATEGY} or the default {@link AsyncHealthCheckInvocationStrategy}</li>
 </ul>
 *
 * <h4>Extension points</h4>
 *
 * The default behavior of bootstrapping can be extended by extending this class and overriding the following as needed:
 * <ul>
 <li>{@link com.netflix.karyon.server.ServerBootstrap#newLifecycleInjectorBuilder()}: Callback to use a custom
 {@link LifecycleInjectorBuilder}</li>
 <li>{@link ServerBootstrap#configureBootstrapBinder(BootstrapBinder)}: Callback to configure {@link BootstrapBinder}
 before returning from {@link com.google.inject.Module#configure(Binder)}.</li>
 <li>{@link ServerBootstrap#configureBinder}: Callback to configure {@link Binder} before returning from
 {@link BootstrapModule#configure(com.netflix.governator.guice.BootstrapBinder)}.</li>
 <li>{@link ServerBootstrap#beforeInjectorCreation(com.netflix.governator.guice.LifecycleInjectorBuilder)}: A callback
 before creating the {@link com.google.inject.Injector} from {@link LifecycleInjectorBuilder} provided by this class
 to {@link KaryonServer}</li>
 <li>{@link com.netflix.karyon.server.ServerBootstrap#getBasePackages()}: Specify the base packages to be added for
 governator classpath scanning. This is in case for any reason one does not want to specify a property
 {@link PropertyNames#SERVER_BOOTSTRAP_BASE_PACKAGES_OVERRIDE} as mentioned above.</li>
 <li>{@link com.netflix.karyon.server.ServerBootstrap#getConfigurationProvider()}: Any custom configuration provider
 that is to be used by governator. Defaults to {@link ArchaiusConfigurationProvider}. This can be used to make karyon
 not register any configuration provider for governator, by returning <code>null</code> from this method.</li>
 </ul>
 *
 * In case, the above extension is required, the name of the custom class must be specified in a system property named
 * {@link PropertyNames#SERVER_BOOTSTRAP_CLASS_OVERRIDE}. eg: -Dcom.netflix.karyon.server.bootstrap.class=com.mycompany.MyBootsrap
 *
 * @author Nitesh Kant
 */
public class ServerBootstrap {

    private static final Logger logger = LoggerFactory.getLogger(ServerBootstrap.class);

    private Set<String> allBasePackages;
    private ClasspathScanner classpathScanner;

    void initialize() {
        readBasePackages();

        List<Class<? extends Annotation>> annotations = Lists.newArrayList();
        annotations.add(Application.class);
        annotations.add(Component.class);

        logger.info("Creating a new governator classpath scanner with base packages: " + allBasePackages);
        classpathScanner = LifecycleInjector.createStandardClasspathScanner(allBasePackages, annotations);
    }

    LifecycleInjectorBuilder bootstrap() {
        return newLifecycleInjectorBuilder().usingBasePackages(allBasePackages).usingClasspathScanner(classpathScanner).
                withBootstrapModule(getBootstrapModule()).withModules(new KaryonGuiceModule());
    }

    /**
     * Returns a new instance of {@link LifecycleInjectorBuilder} to be used by governator. Defaults to
     * {@link com.netflix.governator.guice.LifecycleInjector#builder()}
     *
     * @return A new instance of {@link LifecycleInjectorBuilder} to be used by governator.
     */
    protected LifecycleInjectorBuilder newLifecycleInjectorBuilder() {
        return LifecycleInjector.builder();
    }

    /**
     * Returns the {@link ConfigurationProvider} to be used by governator. Defaults to {@link ArchaiusConfigurationProvider}
     *
     * @return The class instance of the {@link ConfigurationProvider} to be used, this will be instantiated by governator.
     *         If this is null, then karyon will not register a configuration provider with governator.
     */
    @Nullable
    protected Class<? extends ConfigurationProvider> getConfigurationProvider() {
        return ArchaiusConfigurationProvider.class;
    }

    /**
     * Returns the {@link com.netflix.karyon.spi.ServiceRegistryClient} to be used by karyon.
     * Defaults to {@link com.netflix.karyon.server.eureka.EurekaHandler} unless the property
     * {@code PropertyNames#DISABLE_EUREKA_INTEGRATION} is set to <code>false</code>, in which case, this will return
     * <code>null</code>
     *
     * @return The class instance of the {@link ServiceRegistryClient} to be used, this will be instantiated by governator.
     *         If this is null, then karyon will not register any service handler.
     */
    @Nullable
    protected Class<? extends ServiceRegistryClient> getServiceRegistryClient() {
        if (KaryonUtils.isCoreComponentEnabled(PropertyNames.EUREKA_COMPONENT_NAME)) {
            return EurekaHandler.class;
        } else {
            return null;
        }
    }

    /**
     * A callback before creating the {@link com.google.inject.Injector} from {@link LifecycleInjectorBuilder} provided
     * by this class to {@link KaryonServer}. <p/>
     * Default implementation does nothing, so the overridden methods do not need to call super.
     *
     * @param builderToBeUsed The builder to be used for creating an injector. This builder can be modified/configured
     *                        as required.
     */
    protected void beforeInjectorCreation(@SuppressWarnings("unused") LifecycleInjectorBuilder builderToBeUsed) {
        // No op by default
    }

    /**
     * Callback to configure {@link Binder} before returning from {@link com.google.inject.Module#configure(Binder)}.
     * Default implementation does nothing, so the overridden methods do not need to call super.
     *
     * @param binder The binder as passed to the guice module used by karyon.
     */
    protected void configureBinder(@SuppressWarnings("unused") Binder binder) {
        // No op by default
    }

    /**
     * Callback to configure {@link BootstrapBinder} before returning from
     * {@link BootstrapModule#configure(com.netflix.governator.guice.BootstrapBinder)}.
     * Default implementation does nothing, so the overridden methods do not need to call super.
     *
     * @param bootstrapBinder The bootstrap binder as passed to {@link BootstrapModule#configure(com.netflix.governator.guice.BootstrapBinder)}
     */
    protected void configureBootstrapBinder(@SuppressWarnings("unused") BootstrapBinder bootstrapBinder) {
        // No op by default
    }

    /**
     * Creates a new bootstrap module. It is recommended to extend the {@link KaryonBootstrapModule} as your bootstrap
     * module.
     *
     * @return Bootstrap module instance.
     */
    protected BootstrapModule getBootstrapModule() {
        return new KaryonBootstrapModule();
    }

    /**
     * Specify the base packages to be added for governator classpath scanning. This is in case for any reason one does
     * not want to specify a property {@link PropertyNames#SERVER_BOOTSTRAP_BASE_PACKAGES_OVERRIDE} as mentioned in
     * {@link ServerBootstrap}.
     *
     * @return The base package names for governator scanning.
     */
    @Nullable
    protected Collection<String> getBasePackages() {
        List<String> toReturn = new ArrayList<String>();
        List<Object> basePackages = ConfigurationManager.getConfigInstance().getList(
                PropertyNames.SERVER_BOOTSTRAP_BASE_PACKAGES_OVERRIDE);
        for (Object basePackage : basePackages) {
            toReturn.add(String.valueOf(basePackage));
        }
        return toReturn;
    }

    /**
     * Returns the {@link ClasspathScanner} used (to be used) by karyon. This is just for usage by classes outside of
     * karyon, karyon does not use this method to get hold of the scanner. What this essentially means is that
     * overriding this will not yield any benefits.
     *
     * @return The {@link ClasspathScanner} used (to be used) by karyon. This can be null, if this method is called
     * before creating an instance of {@link KaryonServer}
     */
    @Nullable
    @SuppressWarnings("unused")
    protected ClasspathScanner getClasspathScanner() {
        return classpathScanner;
    }

    private void readBasePackages() {
        Set<String> _allBasePackages = new HashSet<String>();
        _allBasePackages.add("com.netflix");

        Collection<String> basePackages = getBasePackages();
        if (null != basePackages) {
            _allBasePackages.addAll(basePackages);
        }

        allBasePackages = _allBasePackages;
    }

    protected class KaryonBootstrapModule implements BootstrapModule {

        @Override
        public void configure(BootstrapBinder binder) {
            Class<? extends ConfigurationProvider> configurationProvider = getConfigurationProvider();
            if (null != configurationProvider) {
                binder.bindConfigurationProvider().to(configurationProvider);
            }
            configureBootstrapBinder(binder);
        }
    }

    public class KaryonGuiceModule extends HealthCheckModule {

        @Override
        public void configure() {

            super.configure();

            bindServiceRegistryClient();

            configureBinder(binder());
        }

        protected void bindServiceRegistryClient() {
            Class<? extends ServiceRegistryClient> serviceRegistryClient = getServiceRegistryClient();
            if (null != serviceRegistryClient) {
                binder().bind(ServiceRegistryClient.class).to(serviceRegistryClient);
                logger.info("Bound service registry client to: " + serviceRegistryClient.getName());
            } else {
                logger.info("Not registering a service registry client. This may be 1) If you return null from " +
                            "getServiceRegistryClient() in your " + ServerBootstrap.class.getName() + " implementation." +
                            " 2) If you have set the property: " + PropertyNames.DISABLE_EUREKA_INTEGRATION + " to true.");
                binder().bind(ServiceRegistryClient.class).to(NoneServiceRegistryClient.class);
            }
        }

    }

}
