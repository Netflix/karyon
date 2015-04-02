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

import com.google.inject.*;
import com.netflix.config.ConfigurationManager;
import com.netflix.governator.guice.LifecycleInjector;
import com.netflix.governator.guice.LifecycleInjectorMode;
import com.netflix.governator.lifecycle.LifecycleManager;

import netflix.admin.AdminConfigImpl;
import netflix.admin.AdminContainerConfig;

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

import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicBoolean;

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
 * <li>Healthcheck: A healthcheck is available with {@link netflix.admin.HealthCheckServlet}.
 * </ul>
 */
public class AdminResourcesContainer {
    private static final Logger logger = LoggerFactory.getLogger(AdminResourcesContainer.class);

    public static final String DEFAULT_PAGE_PROP_NAME = "com.netflix.karyon.admin.default.page";

    public static final String CONTAINER_LISTEN_PORT = "netflix.platform.admin.resources.port";
    public static final int LISTEN_PORT_DEFAULT = 8077;
    private static final String JERSEY_CORE_PACKAGES = "netflix.platform.admin.resources.core.packages";
    public static final String JERSEY_CORE_PACKAGES_DEFAULT = "netflix.adminresources;com.netflix.explorers.resources;com.netflix.explorers.providers";
    public static final String JERSEY_PACKAGES_ADMIN_TEMPLATES = "netflix.admin;netflix.adminresources.pages;com.netflix.explorers.resources";

    private String coreJerseyPackages = ConfigurationManager.getConfigInstance().getString(JERSEY_CORE_PACKAGES, JERSEY_CORE_PACKAGES_DEFAULT);
    private int listenPort = ConfigurationManager.getConfigInstance().getInt(CONTAINER_LISTEN_PORT, LISTEN_PORT_DEFAULT);
    private Server server;

    @Inject(optional = true)
    private Injector appInjector;

    private AtomicBoolean alreadyInited = new AtomicBoolean(false);

    /**
     * Starts the container and hence the embedded jetty server.
     *
     * @throws Exception if there is an issue while starting the server
     */
    @PostConstruct
    public void init() throws Exception {
        try {

            if (alreadyInited.compareAndSet(false, true)) {

                server = new Server(listenPort);

                Injector adminResourceInjector;
                if (appInjector != null) {
                    adminResourceInjector = appInjector.createChildInjector(getAdditionalBindings());
                } else {
                    adminResourceInjector = LifecycleInjector
                            .builder()
                            .inStage(Stage.DEVELOPMENT)
                            .withMode(LifecycleInjectorMode.SIMULATED_CHILD_INJECTORS)
                            .usingBasePackages("com.netflix.explorers")
                            .withModules(getAdditionalBindings())
                            .build()
                            .createInjector();
                    adminResourceInjector.getInstance(LifecycleManager.class).start();
                }

                final AdminPageRegistry adminPageRegistry = buildAdminPageRegistry(adminResourceInjector);
                final AdminContainerConfig adminContainerConfig = adminResourceInjector.getInstance(AdminContainerConfig.class);

                // root path handling, redirect filter
                ServletContextHandler rootHandler = new ServletContextHandler();
                rootHandler.setContextPath("/");
                rootHandler.addFilter(new FilterHolder(adminResourceInjector.getInstance(RedirectFilter.class)), "/*", EnumSet.allOf(DispatcherType.class));
                rootHandler.addServlet(new ServletHolder(new DefaultServlet()), "/*");

                // admin page template resources
                AdminResourcesFilter arfTemplatesResources = adminResourceInjector.getInstance(AdminResourcesFilter.class);
                arfTemplatesResources.setPackages(JERSEY_PACKAGES_ADMIN_TEMPLATES);

                ServletContextHandler adminTemplatesResHandler = new ServletContextHandler();
                adminTemplatesResHandler.setContextPath(adminContainerConfig.templateResourceContext());
                adminTemplatesResHandler.setSessionHandler(new SessionHandler());
                adminTemplatesResHandler.addFilter(LoggingFilter.class, "/*", EnumSet.allOf(DispatcherType.class));
                adminTemplatesResHandler.addFilter(new FilterHolder(arfTemplatesResources), "/*", EnumSet.allOf(DispatcherType.class));
                adminTemplatesResHandler.addServlet(new ServletHolder(new DefaultServlet()), "/*");

                // admin page data resources
                final String jerseyPkgListForAjaxResources = appendCoreJerseyPackages(adminPageRegistry.buildJerseyResourcePkgListForAdminPages());
                AdminResourcesFilter arfDataResources = adminResourceInjector.getInstance(AdminResourcesFilter.class);
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
            }
        } catch (Exception e) {
            logger.error("Exception in building AdminResourcesContainer ", e);
        }
    }

    public int getServerPort() {
        return listenPort;
    }

    private Module getAdditionalBindings() {
        return new AbstractModule() {
            @Override
            protected void configure() {
                bind(AdminContainerConfig.class).to(AdminConfigImpl.class);
                bind(AdminResourcesFilter.class);
            }
        };
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
            if (server != null) {
                server.stop();
            }
        } catch (Throwable t) {
            logger.warn("Error while shutting down Admin resources server", t);
        } finally {
            alreadyInited.set(false);
        }
    }
}

