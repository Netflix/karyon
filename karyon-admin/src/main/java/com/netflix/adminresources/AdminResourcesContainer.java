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

package com.netflix.adminresources;

import com.google.common.base.Joiner;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceFilter;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;
import com.netflix.governator.annotations.Configuration;
import com.netflix.governator.guice.LifecycleInjector;
import com.netflix.governator.guice.LifecycleInjectorBuilder;
import com.netflix.governator.lifecycle.LifecycleManager;
import com.netflix.karyon.server.eureka.HealthCheckInvocationStrategy;
import com.netflix.karyon.spi.Component;
import com.netflix.karyon.spi.PropertyNames;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import org.eclipse.jetty.server.DispatcherType;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Map;

/**
 * This class starts an embedded jetty server, listening at port specified by property
 * {@link AdminResourcesContainer#CONTAINER_LISTEN_PORT} and defaulting to
 * {@link AdminResourcesContainer#LISTEN_PORT_DEFAULT}. <br/>
 * The embedded server is a jersey-governator combination so any jersey resources available in packages
 * specified via properties {@link AdminResourcesContainer#JERSEY_CORE_PACKAGES} and
 * {@link AdminResourcesContainer#JERSEY_APP_PACKAGES} will be scanned and initialized via governator. <br/>
 * Karyon admin uses a completely different governator injector from the main karyon server for the purpose of isolation
 * of the main application from the admin application. <br/>
 *
 * Karyon admin starts in an embedded container to have a "always available" endpoint for any application. This helps
 * in a homogeneous admin view for all applications.
 *
 * @author pkamath
 * @author Nitesh Kant
 * @author Jordan Zimmerman
 */
@Component(disableProperty = "netflix.platform.admin.resources.disable")
public class AdminResourcesContainer {

    private static final Logger logger = LoggerFactory.getLogger(AdminResourcesContainer.class);

    public static final String DEFAULT_PAGE_PROP_NAME = PropertyNames.KARYON_PROPERTIES_PREFIX + "admin.default.page";

    public static final DynamicStringProperty DEFAULT_PAGE =
            DynamicPropertyFactory.getInstance().getStringProperty(DEFAULT_PAGE_PROP_NAME, "/healthcheck");

    public static final String CONTAINER_LISTEN_PORT = "netflix.platform.admin.resources.port";
    public static final int LISTEN_PORT_DEFAULT = 8077;

    public static final String JERSEY_CORE_PACKAGES = "netflix.platform.admin.resources.core.packages";
    public static final String JERSEY_CORE_PACKAGES_DEAULT = "com.netflix.adminresources";

    public static final String JERSEY_APP_PACKAGES = "netflix.platform.admin.resources.packages";

    @Configuration(
            value = CONTAINER_LISTEN_PORT,
            documentation = "Property defining the listen port for admin resources.",
            ignoreTypeMismatch = true
    )
    private int listenPort = LISTEN_PORT_DEFAULT;

    @Configuration(
            value = JERSEY_CORE_PACKAGES,
            documentation = "Property defining the list of core packages which contains jersey resources for karyon admin. com.netflix.adminresources is always added to this."
    )
    private String coreJerseyPackages = JERSEY_CORE_PACKAGES_DEAULT;

    @Configuration(
            value = JERSEY_APP_PACKAGES,
            documentation = "Property defining the list of additional packages which contains jersey resources for karyon admin. This will be on top of the core packages."
    )
    private String appJerseyPackages = "";
    private Server server;

    private final LifecycleInjectorBuilder builder;

    @Inject
    public AdminResourcesContainer(final HealthCheckInvocationStrategy healthCheckInvocationStrategy) {
        final String packages = Joiner.on(",").join(getAdminResourcePackages());
        builder = LifecycleInjector.builder().usingBasePackages(packages).withModules(
                new JerseyServletModule() {
                    @Override
                    protected void configureServlets() {
                        Map<String, String> params = Maps.newHashMap();
                        params.put(PackagesResourceConfig.PROPERTY_PACKAGES, packages);
                        serve("/*").with(GuiceContainer.class, params);
                        filter("/*").through(LoggingFilter.class);
                        binder().bind(GuiceContainer.class).asEagerSingleton();
                        binder().bind(HealthCheckInvocationStrategy.class).toInstance(healthCheckInvocationStrategy); // we use the only instance as async strategies have an overhead
                    }

                }
        );
        final Injector injector = builder.createInjector();
        LifecycleManager manager = injector.getInstance(LifecycleManager.class);
        try {
            manager.start();
        } catch (Exception e) {
            logger.error("Governator lifecycle manager failed to start for admin resources, admin resources will not be started.", e);
            throw Throwables.propagate(e);
        }

        server = new Server(listenPort);
        ServletContextHandler handler = new ServletContextHandler();
        handler.setContextPath("/");
        handler.addServlet(new ServletHolder(new RedirectServlet()), "/*");
        FilterHolder guiceFilter = new FilterHolder(injector.getInstance(GuiceFilter.class));
        handler.addFilter(guiceFilter, "/*", EnumSet.allOf(DispatcherType.class));

        server.setHandler(handler);
    }

    private Iterable getAdminResourcePackages() {
        return Arrays.asList(coreJerseyPackages, appJerseyPackages);
    }

    /**
     * Starts the container and hence the embedded jetty server.
     *
     * @throws Exception if there is an issue while starting the server
     */
    @PostConstruct
    public void init() throws Exception {
        if (!coreJerseyPackages.equals(JERSEY_CORE_PACKAGES_DEAULT)) {
            coreJerseyPackages = Joiner.on(",").join(coreJerseyPackages, "com.netflix.adminresources"); // duplication does not hurt.
        }
        server.start();
    }

    @PreDestroy
    public void shutdown() {
        try {
            server.stop();
        } catch (Throwable t) {
            logger.warn("Error while shutting down Admin resources server", t);
        }
    }
}
