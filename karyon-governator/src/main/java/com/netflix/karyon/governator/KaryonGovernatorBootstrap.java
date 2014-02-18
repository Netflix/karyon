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

package com.netflix.karyon.governator;

import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.netflix.governator.guice.BootstrapModule;
import com.netflix.governator.guice.LifecycleInjector;
import com.netflix.governator.guice.LifecycleInjectorBuilder;
import com.netflix.governator.lifecycle.ClasspathScanner;
import com.netflix.karyon.server.KaryonServer;
import com.netflix.karyon.server.bootstrap.AlwaysHealthyHealthCheck;
import com.netflix.karyon.server.bootstrap.HealthCheckHandler;
import com.netflix.karyon.server.bootstrap.HealthCheckInvocationStrategy;
import com.netflix.karyon.server.bootstrap.KaryonBootstrap;
import com.netflix.karyon.server.bootstrap.NoneServiceRegistryClient;
import com.netflix.karyon.server.bootstrap.ServiceRegistryClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * An implementation of {@link KaryonBootstrap} that supports Dependency Injection via
 * <a href="https://github.com/Netflix/governator/">Governator</a>. <br/>
 *
 * The only way to create an instance of this class is via the provided {@link Builder}
 *
 * @author Nitesh Kant
 */
public class KaryonGovernatorBootstrap implements KaryonBootstrap {

    private static final Logger logger = LoggerFactory.getLogger(KaryonGovernatorBootstrap.class);

    private final Set<String> allBasePackages;
    private final ClasspathScanner classpathScanner;
    private final LifecycleInjectorBuilder lifecycleInjectorBuilder;
    private Injector injector;

    protected KaryonGovernatorBootstrap(LifecycleInjectorBuilder lifecycleInjectorBuilder, String... scanPackages) {
        this.lifecycleInjectorBuilder = lifecycleInjectorBuilder;
        allBasePackages = sanitizeBasePackages(scanPackages);
        List<Class<? extends Annotation>> annotations = Lists.newArrayList();
        annotations.add(Application.class);
        annotations.add(Component.class);

        logger.info("Creating a new governator classpath scanner with base packages: " + allBasePackages);
        classpathScanner = LifecycleInjector.createStandardClasspathScanner(allBasePackages, annotations);
    }

    @Override
    public void bootstrap() {
        lifecycleInjectorBuilder.usingBasePackages(allBasePackages).usingClasspathScanner(classpathScanner);
        injector = createInjector(lifecycleInjectorBuilder);
    }

    @Nullable
    @Override
    public HealthCheckHandler healthcheckHandler() {
        return injector.getInstance(HealthCheckHandler.class);
    }

    @Nullable
    @Override
    public HealthCheckInvocationStrategy healthCheckInvocationStrategy() {
        return injector.getInstance(HealthCheckInvocationStrategy.class);
    }

    @Nullable
    @Override
    public ServiceRegistryClient serviceRegistryClient() {
        return injector.getInstance(ServiceRegistryClient.class);
    }

    public Injector getInjector() {
        return injector;
    }

    /**
     * Returns the {@link ClasspathScanner} used by karyon.
     *
     * @return The {@link ClasspathScanner} used by karyon.
     */
    public ClasspathScanner getClasspathScanner() {
        return classpathScanner;
    }

    /**
     * A callback before creating the {@link Injector} from {@link LifecycleInjectorBuilder} provided
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
     * create the main application injector
     */
    protected Injector createInjector(LifecycleInjectorBuilder builder) {
        beforeInjectorCreation(builder);
        LifecycleInjector lifecycleInjector = builder.build();
        Injector injector = lifecycleInjector.createInjector();
        return injector;
    }

    private static Set<String> sanitizeBasePackages(String[] scanPackages) {
        Set<String> _allBasePackages = new HashSet<String>();
        _allBasePackages.add("com.netflix");

        if (null == scanPackages) {
            return _allBasePackages;
        }

        Collections.addAll(_allBasePackages, scanPackages);
        return _allBasePackages;
    }

    public static class Builder {

        private final String[] scanPackages;
        private Class<? extends ServiceRegistryClient> serviceRegistryClient;
        private Class<? extends HealthCheckHandler> handlerClass;
        private Class<? extends HealthCheckInvocationStrategy> strategyClass;
        private final LifecycleInjectorBuilder lifecycleInjectorBuilder;

        public Builder(String... scanPackages) {
            this(LifecycleInjector.builder().withBootstrapModule(new KaryonBootstrapModule()), scanPackages);
        }

        /**
         * Use this constructor if you have a customized {@link LifecycleInjectorBuilder}. In this case, none of the
         * defaults are applied to the {@link LifecycleInjectorBuilder}, eg: The {@link BootstrapModule} is not set as
         * {@link KaryonBootstrapModule}
         *
         * @param builder The builder to use.
         * @param scanPackages Packages to scan for governator annotations.
         */
        public Builder(LifecycleInjectorBuilder builder, String... scanPackages) {
            this.scanPackages = scanPackages;
            lifecycleInjectorBuilder = builder;
        }

        public Builder modules(Module... modules) {
            lifecycleInjectorBuilder.withAdditionalModules(modules);
            return this;
        }

        public Builder modules(Set<Module> modules) {
            lifecycleInjectorBuilder.withAdditionalModules(modules);
            return this;
        }

        public Builder serviceRegistryClient(Class<? extends ServiceRegistryClient> clientClass) {
            serviceRegistryClient = clientClass;
            return this;
        }

        public Builder healthCheckHandler(Class<? extends HealthCheckHandler> handlerClass) {
            this.handlerClass = handlerClass;
            return this;
        }

        public Builder healthCheckStrategy(Class<? extends HealthCheckInvocationStrategy> strategyClass) {
            this.strategyClass = strategyClass;
            return this;
        }

        public KaryonGovernatorBootstrap build() {
            if (null != serviceRegistryClient || null != handlerClass || null != strategyClass) {
                lifecycleInjectorBuilder.withAdditionalModules(new AbstractModule() {
                    @Override
                    protected void configure() {
                        if (null != serviceRegistryClient) {
                            bind(ServiceRegistryClient.class).to(serviceRegistryClient);
                        } else {
                            bind(ServiceRegistryClient.class).to(NoneServiceRegistryClient.class);
                        }
                        if (null != handlerClass) {
                            bind(HealthCheckHandler.class).to(handlerClass);
                        } else {
                            bind(HealthCheckHandler.class).toInstance(AlwaysHealthyHealthCheck.INSTANCE);
                        }
                        if (null != strategyClass) {
                            bind(HealthCheckInvocationStrategy.class).to(strategyClass);
                        } else {
                            bind(HealthCheckInvocationStrategy.class).to(DefaultHealthCheckInvocationStrategy.class);
                        }
                    }
                });
            }

            return newBootstrapInstance(lifecycleInjectorBuilder, scanPackages);
        }

        protected KaryonGovernatorBootstrap newBootstrapInstance(LifecycleInjectorBuilder lifecycleInjectorBuilder,
                                                                 String[] scanPackages) {
            return new KaryonGovernatorBootstrap(lifecycleInjectorBuilder, scanPackages);
        }
    }
}
