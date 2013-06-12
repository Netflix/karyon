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

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;
import com.netflix.governator.annotations.Configuration;
import com.netflix.governator.guice.LifecycleInjector;
import com.netflix.governator.lifecycle.LifecycleManager;
import com.netflix.karyon.server.eureka.HealthCheckInvocationStrategy;
import com.netflix.karyon.spi.Component;
import com.netflix.karyon.spi.PropertyNames;
import org.eclipse.jetty.server.DispatcherType;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.EnumSet;

/**
 * This class starts an embedded jetty server, listening at port specified by property
 * {@link AdminResourcesContainer#CONTAINER_LISTEN_PORT} and defaulting to
 * {@link AdminResourcesContainer#LISTEN_PORT_DEFAULT}. <br/>
 *
 * The embedded server uses jersey so any jersey resources available in packages
 * specified via properties {@link AdminResourcesContainer#JERSEY_CORE_PACKAGES} and
 * {@link AdminResourcesContainer#JERSEY_APP_PACKAGES} will be scanned and initialized. <br/>
 * <b>This server does not use guice/governator to initialize jersey resources as guice has an
 * <a href="https://code.google.com/p/google-guice/issues/detail?id=635">open issue</a> which makes it difficult to have
 * multiple {@link com.google.inject.servlet.GuiceFilter} in the same JVM.</b>
 *
 * Karyon admin starts in an embedded container to have a "always available" endpoint for any application. This helps
 * in a homogeneous admin view for all applications. <br/>
 *
 * <h3>Available resources</h3>
 *
 * The following resources are available by default:
 *
 * <ul>
 <li>Healthcheck: A healthcheck is available at path {@link HealthCheckServlet#PATH}. This utilizes the configured
 {@link com.netflix.karyon.spi.HealthCheckHandler} for karyon.</li>
 <li>Admin resource: Any url starting with "/adminres" is served via {@link com.netflix.adminresources.resources.EmbeddedContentResource}</li>
 </ul>
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
    private static final String JERSEY_CORE_PACKAGES = "netflix.platform.admin.resources.core.packages";
    public static final String JERSEY_CORE_PACKAGES_DEAULT = "com.netflix.adminresources;com.netflix.explorers.resources;com.netflix.explorers.providers";

    private final Provider<HealthCheckInvocationStrategy> healthCheckInvocationStrategyProvider;

    @Configuration(
            value = JERSEY_CORE_PACKAGES,
            documentation = "Property defining the list of core packages which contains jersey resources for karyon admin. com.netflix.adminresources is always added to this."
    )
    private String coreJerseyPackages = JERSEY_CORE_PACKAGES_DEAULT;


    @Configuration(
            value = CONTAINER_LISTEN_PORT,
            documentation = "Property defining the listen port for admin resources.",
            ignoreTypeMismatch = true
    )
    private int listenPort = LISTEN_PORT_DEFAULT;

    private Server server;

    @Inject
    public AdminResourcesContainer(Provider<HealthCheckInvocationStrategy> healthCheckInvocationStrategyProvider) {
        this.healthCheckInvocationStrategyProvider = healthCheckInvocationStrategyProvider;

    }

    /**
     * Starts the container and hence the embedded jetty server.
     *
     * @throws Exception if there is an issue while starting the server
     */
    @PostConstruct
    public void init() throws Exception {
        server = new Server(listenPort);
        Injector injector = LifecycleInjector
                .builder()
                .usingBasePackages("com.netflix.explorers")
                .withModules(new AdminResourcesModule(healthCheckInvocationStrategyProvider)).createInjector();
        try {
            injector.getInstance(LifecycleManager.class).start();
            AdminResourcesFilter adminResourcesFilter = injector.getInstance(AdminResourcesFilter.class);
            adminResourcesFilter.setPackages(coreJerseyPackages);

            ServletContextHandler handler = new ServletContextHandler();
            handler.setContextPath("/");

            handler.setSessionHandler(new SessionHandler());
            handler.addFilter(LoggingFilter.class, "/*", EnumSet.allOf(DispatcherType.class));
            handler.addFilter(RedirectFilter.class, "/*", EnumSet.allOf(DispatcherType.class));
            handler.addFilter(new FilterHolder(adminResourcesFilter), "/*", EnumSet.allOf(DispatcherType.class));
            handler.addServlet(new ServletHolder(adminResourcesFilter), "/*");

            server.setHandler(handler);
            server.start();
        } catch (Exception e) {
            logger.error("Exception in building AdminResourcesContainer ", e);
        }
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

