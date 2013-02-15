/*
 * Copyright 2013 Netflix, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

package com.netflix.karyon.server;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.netflix.config.ConfigurationManager;
import com.netflix.governator.annotations.AutoBindSingleton;
import com.netflix.governator.configuration.ArchaiusConfigurationProvider;
import com.netflix.governator.guice.BootstrapBinder;
import com.netflix.governator.guice.BootstrapModule;
import com.netflix.governator.guice.LifecycleInjector;
import com.netflix.governator.guice.LifecycleInjectorBuilder;
import com.netflix.governator.lifecycle.ClasspathScanner;
import com.netflix.karyon.spi.Application;
import com.netflix.karyon.spi.Component;
import com.netflix.karyon.spi.HealthCheckHandler;
import com.netflix.karyon.spi.PropertyNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
 </ul>
 *
 * <h4>Extension points</h4>
 *
 * The default behavior of bootstrapping can be extended by extending this class and overriding the following as needed:
 * <ul>
 <li>{@link ServerBootstrap#configureBootstrapBinder(com.netflix.governator.guice.BootstrapBinder)}: Callback to configure
 {@link BootstrapBinder} before returning from {@link BootstrapModule#configure(com.netflix.governator.guice.BootstrapBinder)}</li>
 <li>{@link ServerBootstrap#beforeInjectorCreation(com.netflix.governator.guice.LifecycleInjectorBuilder)}: A callback
 before creating the {@link com.google.inject.Injector} from {@link LifecycleInjectorBuilder} provided by this class
 to {@link KaryonServer}</li>
 <li>{@link com.netflix.karyon.server.ServerBootstrap#getBasePackages()}: Specify the base packages to be added for
 governator classpath scanning. This is in case for any reason one does not want to specify a property
 {@link PropertyNames#SERVER_BOOTSTRAP_BASE_PACKAGES_OVERRIDE} as mentioned above.</li>
 </ul>
 *
 * In case, the above extension is required, the name of the custom class must be specified in a system property named
 * {@link PropertyNames#SERVER_BOOTSTRAP_CLASS_OVERRIDE}. eg: -Dcom.netflix.karyon.server.bootstrap.class=com.mycompany.MyBootsrap
 *
 * @author Nitesh Kant (nkant@netflix.com)
 */
public class ServerBootstrap {

    private static final Logger logger = LoggerFactory.getLogger(ServerBootstrap.class);

    LifecycleInjectorBuilder bootstrap() {
        List<String> allBasePackages = new ArrayList<String>();
        allBasePackages.add("com.netflix");

        Collection<String> basePackages = getBasePackages();
        if (null != basePackages) {
            allBasePackages.addAll(basePackages);
        }

        // TODO: Replace with governator native call when fix for https://github.com/Netflix/governator/issues/43 is available
        List<Class<? extends Annotation>> annotations = Lists.newArrayList();
        annotations.add(AutoBindSingleton.class);
        annotations.add(Inject.class);
        annotations.add(javax.inject.Inject.class);
        annotations.add(Application.class);
        annotations.add(Component.class);
        ClasspathScanner scanner = new ClasspathScanner(basePackages, annotations);

        return LifecycleInjector.builder().usingBasePackages(allBasePackages).usingClasspathScanner(
                scanner).withBootstrapModule(new BootstrapModuleImpl());
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

    private class BootstrapModuleImpl implements BootstrapModule {

        @Override
        public void configure(BootstrapBinder binder) {

            bindHealthCheckHandler(binder);

            binder.bindConfigurationProvider().to(ArchaiusConfigurationProvider.class);

            configureBootstrapBinder(binder);
        }

        @SuppressWarnings("unchecked")
        private void bindHealthCheckHandler(BootstrapBinder binder) {
            String healthCheckHandlerClass = ConfigurationManager.getConfigInstance()
                                                .getString(PropertyNames.HEALTH_CHECK_HANDLER_CLASS_PROP_NAME);
            boolean bound = false;
            if (null != healthCheckHandlerClass) {
                Class<? extends HealthCheckHandler> handlerClass = null;
                try {
                    Class<?> aClass = Class.forName(healthCheckHandlerClass);
                    if (HealthCheckHandler.class.isAssignableFrom(aClass)) {
                        binder.bind(HealthCheckHandler.class).to((Class<? extends HealthCheckHandler>) aClass);
                        bound = true;
                    } else {
                        logger.warn(String.format(
                                "Health check handler %s specified does not implement %s. This handler will not be registered with karyon.",
                                healthCheckHandlerClass, HealthCheckHandler.class.getName()));
                    }
                } catch (ClassNotFoundException e) {
                    logger.error(String.format(
                            "Handler class %s specified as property %s can not be found. This handler will not be registered with karyon.",
                            handlerClass, PropertyNames.HEALTH_CHECK_HANDLER_CLASS_PROP_NAME), e);
                }
            } else {
                logger.info("No health check handler defined. This means your application can not provide meaningful health state to external entities. " +
                            "It is highly recommended that you provide an implementation of " + HealthCheckHandler.class.getName() +
                            " and specify the fully qualified class name of the implementation in the property " + PropertyNames.HEALTH_CHECK_HANDLER_CLASS_PROP_NAME);
            }

            if(!bound) {
                binder.bind(HealthCheckHandler.class).toInstance(new HealthCheckHandler() {
                    @Override
                    public int checkHealth() {
                        return 200;
                    }
                });
            }
        }
    }
}
