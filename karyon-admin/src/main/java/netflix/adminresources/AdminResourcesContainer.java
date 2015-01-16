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

package netflix.adminresources;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.netflix.config.ConfigurationManager;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;
import com.netflix.governator.guice.LifecycleInjector;
import com.netflix.governator.lifecycle.LifecycleManager;
import netflix.admin.AdminConfigImpl;
import netflix.admin.AdminContainerConfig;
import netflix.admin.HealthCheckServlet;
import netflix.karyon.health.HealthCheckHandler;
import netflix.karyon.health.HealthCheckInvocationStrategy;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.DispatcherType;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import java.util.EnumSet;

/**
 * This class starts an embedded jetty server, listening at port specified by property
 * {@link AdminResourcesContainer#CONTAINER_LISTEN_PORT} and defaulting to
 * {@link AdminResourcesContainer#LISTEN_PORT_DEFAULT}. <br>
 * <p/>
 * The embedded server uses jersey so any jersey resources available in packages
 * specified via properties {@link AdminResourcesContainer#JERSEY_CORE_PACKAGES}will be scanned and initialized. <br>
 * <p/>
 * Karyon admin starts in an embedded container to have a "always available" endpoint for any application. This helps
 * in a homogeneous admin view for all applications. <br>
 * <p/>
 * <h3>Available resources</h3>
 * <p/>
 * The following resources are available by default:
 * <p/>
 * <ul>
 * <li>Healthcheck: A healthcheck is available at path {@link netflix.admin.HealthCheckServlet}. This utilizes the configured
 * {@link HealthCheckHandler} for karyon.</li>
 * </ul>
 *
 */
public class AdminResourcesContainer {
    private static final Logger logger = LoggerFactory.getLogger(AdminResourcesContainer.class);

    public static final String DEFAULT_PAGE_PROP_NAME = "com.netflix.karyon.admin.default.page";

    public static final DynamicStringProperty DEFAULT_PAGE =
            DynamicPropertyFactory.getInstance().getStringProperty(DEFAULT_PAGE_PROP_NAME, "/healthcheck");

    public static final String CONTAINER_LISTEN_PORT = "netflix.platform.admin.resources.port";
    public static final int LISTEN_PORT_DEFAULT = 8077;
    private static final String JERSEY_CORE_PACKAGES = "netflix.platform.admin.resources.core.packages";
    public static final String JERSEY_CORE_PACKAGES_DEFAULT = "netflix.adminresources;com.netflix.explorers.resources;com.netflix.explorers.providers";
    public static final String JERSEY_PACKAGES_ADMIN_TEMPLATES = "netflix.admin;netflix.adminresources.pages;com.netflix.explorers.resources";

    private String coreJerseyPackages = ConfigurationManager.getConfigInstance().getString(JERSEY_CORE_PACKAGES, JERSEY_CORE_PACKAGES_DEFAULT);
    private int listenPort = ConfigurationManager.getConfigInstance().getInt(CONTAINER_LISTEN_PORT, LISTEN_PORT_DEFAULT);
    private Server server;

    private final Provider<HealthCheckInvocationStrategy> strategy;
    private final Provider<HealthCheckHandler> handlerProvider;

    @Inject
    public AdminResourcesContainer(Provider<HealthCheckInvocationStrategy> strategy,
                                   Provider<HealthCheckHandler> handlerProvider) {
        this.strategy = strategy;
        this.handlerProvider = handlerProvider;
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
                .withModules(new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(HealthCheckInvocationStrategy.class).toProvider(strategy);
                        bind(HealthCheckHandler.class).toProvider(handlerProvider);
                        bind(AdminContainerConfig.class).to(AdminConfigImpl.class);
                        bind(AdminResourcesFilter.class);
                    }
                })
                .build()
                .createInjector();
        injector.getInstance(LifecycleManager.class).start();

        try {
            final AdminPageRegistry adminPageRegistry = buildAdminPageRegistry(injector);
            final AdminContainerConfig adminContainerConfig = injector.getInstance(AdminContainerConfig.class);
            final HealthCheckServlet healthCheckServlet = injector.getInstance(HealthCheckServlet.class);

            // root redirection, health-check servlet
            ServletContextHandler rootHandler = new ServletContextHandler();
            rootHandler.setContextPath("/");
            rootHandler.addFilter(RedirectFilter.class, "/", EnumSet.allOf(DispatcherType.class));
            rootHandler.addServlet(new ServletHolder(healthCheckServlet), adminContainerConfig.healthCheckPath());
            rootHandler.addServlet(new ServletHolder(new DefaultServlet()), "/*");

            // admin page template resources
            AdminResourcesFilter arfTemplatesResources = injector.getInstance(AdminResourcesFilter.class);
            arfTemplatesResources.setPackages(JERSEY_PACKAGES_ADMIN_TEMPLATES);

            ServletContextHandler adminTemplatesResHandler = new ServletContextHandler();
            adminTemplatesResHandler.setContextPath(adminContainerConfig.templateResourceContext());
            adminTemplatesResHandler.setSessionHandler(new SessionHandler());
            adminTemplatesResHandler.addFilter(LoggingFilter.class, "/*", EnumSet.allOf(DispatcherType.class));
            adminTemplatesResHandler.addFilter(new FilterHolder(arfTemplatesResources), "/*", EnumSet.allOf(DispatcherType.class));
            adminTemplatesResHandler.addServlet(new ServletHolder(new DefaultServlet()), "/*");

            // admin page data resources
            final String jerseyPkgListForAjaxResources = appendCoreJerseyPackages(adminPageRegistry.buildJerseyResourcePkgListForAdminPages());
            AdminResourcesFilter arfDataResources = injector.getInstance(AdminResourcesFilter.class);
            arfDataResources.setPackages(jerseyPkgListForAjaxResources);

            ServletContextHandler adminDataResHandler = new ServletContextHandler();
            adminDataResHandler.setContextPath(adminContainerConfig.ajaxDataResourceContext());
            adminDataResHandler.addFilter(new FilterHolder(arfDataResources), "/*", EnumSet.allOf(DispatcherType.class));
            adminDataResHandler.addServlet(new ServletHolder(new DefaultServlet()), "/*");

            HandlerCollection handlers = new HandlerCollection();
            handlers.setHandlers(new Handler[]{adminTemplatesResHandler, adminDataResHandler, rootHandler});
            server.setHandler(handlers);
            server.start();

            final Connector connector = server.getConnectors()[0];
            listenPort = connector.getLocalPort();

        } catch (Exception e) {
            logger.error("Exception in building AdminResourcesContainer ", e);
        }
    }

    public int getListenPort() {
        return listenPort;
    }

    private String appendCoreJerseyPackages(String jerseyResourcePkgListForAdminPages) {
        String pkgPath = coreJerseyPackages;
        if (jerseyResourcePkgListForAdminPages != null && !jerseyResourcePkgListForAdminPages.isEmpty()) {
            pkgPath += ";" + jerseyResourcePkgListForAdminPages;
        }

        return pkgPath;
    }

    private AdminPageRegistry buildAdminPageRegistry(Injector injector) {
        AdminPageRegistry baseServerPageRegistry = injector.getInstance(AdminPageRegistry.class);
        baseServerPageRegistry.registerAdminPagesWithClasspathScan();
        return baseServerPageRegistry;
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

