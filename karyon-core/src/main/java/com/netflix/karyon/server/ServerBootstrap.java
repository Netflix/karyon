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
import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.netflix.config.ConfigurationManager;
import com.netflix.governator.configuration.ArchaiusConfigurationProvider;
import com.netflix.governator.guice.BootstrapBinder;
import com.netflix.governator.guice.BootstrapModule;
import com.netflix.governator.guice.LifecycleInjector;
import com.netflix.governator.guice.LifecycleInjectorBuilder;
import com.netflix.governator.lifecycle.ClasspathScanner;
import com.netflix.karyon.server.eureka.AsyncHealthCheckInvocationStrategy;
import com.netflix.karyon.server.eureka.HealthCheckInvocationStrategy;
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
 <li>{@link ServerBootstrap#configureBinder}: Callback to configure
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
 * @author Nitesh Kant
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

        List<Class<? extends Annotation>> annotations = Lists.newArrayList();
        annotations.add(Application.class);
        annotations.add(Component.class);
        ClasspathScanner scanner = LifecycleInjector.createStandardClasspathScanner(allBasePackages, annotations);
        return LifecycleInjector.builder().usingBasePackages(allBasePackages).usingClasspathScanner(
                scanner).withBootstrapModule(new KaryonBootstrapModule()).withModules(
                new KaryonGuiceModule());
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

    private static class KaryonBootstrapModule implements BootstrapModule {

        @Override
        public void configure(BootstrapBinder binder) {
            binder.bindConfigurationProvider().to(ArchaiusConfigurationProvider.class);
        }
    }

    private class KaryonGuiceModule extends AbstractModule {

        @Override
        public void configure() {

            bindHealthCheckStrategy(binder());

            bindHealthCheckHandler(binder());

            configureBinder(binder());
        }

        private void bindHealthCheckStrategy(Binder binder) {
            boolean bound = bindACustomClass(binder, PropertyNames.HEALTH_CHECK_STRATEGY,
                    HealthCheckHandler.class,
                    "No health check invocation strategy specified, using the default strategy %s. In order to override " +
                    "this behavior you provide an implementation of %s and specify the fully qualified class name of " +
                    "the implementation in the property %s", AsyncHealthCheckInvocationStrategy.class.getName(),
                    HealthCheckInvocationStrategy.class.getName(), PropertyNames.HEALTH_CHECK_STRATEGY);

            if(!bound) {
                binder.bind(HealthCheckInvocationStrategy.class).to(AsyncHealthCheckInvocationStrategy.class);
            }
        }

        private void bindHealthCheckHandler(Binder binder) {
            boolean bound = bindACustomClass(binder, PropertyNames.HEALTH_CHECK_HANDLER_CLASS_PROP_NAME,
                    HealthCheckHandler.class,
                    "No health check handler defined. This means your application can not provide meaningful health " +
                    "state to external entities. It is highly recommended that you provide an implementation of %s and " +
                    "specify the fully qualified class name of the implementation in the property %s",
                    HealthCheckHandler.class.getName(), PropertyNames.HEALTH_CHECK_HANDLER_CLASS_PROP_NAME);

            if(!bound) {
                binder.bind(HealthCheckHandler.class).toInstance(new HealthCheckHandler() {
                    @Override
                    public int getStatus() {
                        return 200;
                    }
                });
            }
        }

        @SuppressWarnings("unchecked")
        private <T> boolean bindACustomClass(Binder binder, String customClassPropName, Class<T> bindTo,
                                             String propertNotFoundErrMsg, Object... arguments) {
            boolean bound = false;
            String customClassName = ConfigurationManager.getConfigInstance().getString(customClassPropName);
            if (null != customClassName) {
                Class<? extends T> customClass = null;
                try {
                    Class<?> aClass = Class.forName(customClassName);
                    if (bindTo.isAssignableFrom(aClass)) {
                        binder.bind(bindTo).to((Class<? extends T>) aClass);
                        bound = true;
                    } else {
                        logger.warn(String.format("Binding for %s failed, %s can not be assigned to %s.",
                                bindTo.getName(), customClassName, bindTo.getName()));
                    }
                } catch (ClassNotFoundException e) {
                    logger.error(
                            String.format("Binding for %s failed, class %s specified as property %s can not be found.",
                                    bindTo.getName(), customClass, customClassPropName), e);
                }
            } else {
                logger.info(String.format(propertNotFoundErrMsg, arguments));
            }

            return bound;
        }
    }
}
