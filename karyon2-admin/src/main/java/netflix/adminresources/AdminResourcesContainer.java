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
import com.netflix.governator.guice.LifecycleInjector;
import com.netflix.governator.guice.LifecycleInjectorMode;
import com.netflix.governator.lifecycle.LifecycleManager;
import netflix.admin.AdminConfigImpl;
import netflix.admin.AdminContainerConfig;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
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
import javax.servlet.DispatcherType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class starts an embedded jetty server, listening at port specified by property
 * {@link netflix.admin.AdminContainerConfig#listenPort()} and defaulting to
 * {@link netflix.admin.AdminContainerConfig}. <br>
 * <p/>
 * The embedded server uses jersey so any jersey resources available in packages
 * specified via properties {@link netflix.admin.AdminContainerConfig#jerseyResourcePkgList()}will be scanned and initialized. <br>
 * <p/>
 * Karyon admin starts in an embedded container to have a "always available" endpoint for any application. This helps
 * in a homogeneous admin view for all applications. <br>
 * <p/>
 * <h3>Available resources</h3>
 * <p/>
 * The following resources are available by default:
 * <p/>
 * </ul>
 */
@Singleton
public class AdminResourcesContainer {
    private static final Logger logger = LoggerFactory.getLogger(AdminResourcesContainer.class);

    /**
     * @deprecated here for backwards compatibility. Use {@link AdminConfigImpl#CONTAINER_LISTEN_PORT}.
     */
    @Deprecated
    public static final String CONTAINER_LISTEN_PORT = AdminConfigImpl.CONTAINER_LISTEN_PORT;

    private Server server;

    @Inject(optional = true)
    private Injector appInjector;

    @Inject(optional = true)
    private AdminContainerConfig adminContainerConfig;

    @Inject(optional = true)
    private AdminPageRegistry adminPageRegistry;

    private AtomicBoolean alreadyInited = new AtomicBoolean(false);
    private int serverPort; // actual server listen port (apart from what's in Config)

    /**
     * Starts the container and hence the embedded jetty server.
     *
     * @throws Exception if there is an issue while starting the server
     */
    @PostConstruct
    public void init() throws Exception {
        try {
            if (alreadyInited.compareAndSet(false, true)) {
                initAdminContainerConfigIfNeeded();
                initAdminRegistryIfNeeded();

                if (adminContainerConfig.shouldScanClassPathForPluginDiscovery()) {
                    adminPageRegistry.registerAdminPagesWithClasspathScan();
                }

                Injector adminResourceInjector;
                if (appInjector != null) {
                    adminResourceInjector = appInjector.createChildInjector(buildAdminPluginsGuiceModules());
                } else {
                    adminResourceInjector = LifecycleInjector
                            .builder()
                            .inStage(Stage.DEVELOPMENT)
                            .withMode(LifecycleInjectorMode.SIMULATED_CHILD_INJECTORS)
                            .usingBasePackages("com.netflix.explorers")
                            .withModules(buildAdminPluginsGuiceModules())
                            .build()
                            .createInjector();
                    adminResourceInjector.getInstance(LifecycleManager.class).start();
                }

                server = new Server(adminContainerConfig.listenPort());

                // redirect filter based on configurable RedirectRules
                ServletContextHandler rootHandler = new ServletContextHandler();
                rootHandler.setContextPath("/");
                rootHandler.addFilter(new FilterHolder(adminResourceInjector.getInstance(RedirectFilter.class)), "/*", EnumSet.allOf(DispatcherType.class));
                rootHandler.addServlet(new ServletHolder(new DefaultServlet()), "/*");

                // admin page template resources
                AdminResourcesFilter arfTemplatesResources = adminResourceInjector.getInstance(AdminResourcesFilter.class);
                arfTemplatesResources.setPackages(adminContainerConfig.jerseyViewableResourcePkgList());

                ServletContextHandler adminTemplatesResHandler = new ServletContextHandler();
                adminTemplatesResHandler.setContextPath(adminContainerConfig.templateResourceContext());
                adminTemplatesResHandler.setSessionHandler(new SessionHandler());
                adminTemplatesResHandler.addFilter(LoggingFilter.class, "/*", EnumSet.allOf(DispatcherType.class));
                adminTemplatesResHandler.addFilter(new FilterHolder(adminResourceInjector.getInstance(RedirectFilter.class)), "/*", EnumSet.allOf(DispatcherType.class));
                adminTemplatesResHandler.addFilter(new FilterHolder(arfTemplatesResources), "/*", EnumSet.allOf(DispatcherType.class));
                adminTemplatesResHandler.addServlet(new ServletHolder(new DefaultServlet()), "/*");

                // admin page data resources
                final String jerseyPkgListForAjaxResources = appendCoreJerseyPackages(adminPageRegistry.buildJerseyResourcePkgListForAdminPages());
                AdminResourcesFilter arfDataResources = adminResourceInjector.getInstance(AdminResourcesFilter.class);
                arfDataResources.setPackages(jerseyPkgListForAjaxResources);

                ServletContextHandler adminDataResHandler = new ServletContextHandler();
                adminDataResHandler.setContextPath(adminContainerConfig.ajaxDataResourceContext());
                adminDataResHandler.addFilter(new FilterHolder(adminResourceInjector.getInstance(RedirectFilter.class)), "/*", EnumSet.allOf(DispatcherType.class));
                adminDataResHandler.addFilter(new FilterHolder(arfDataResources), "/*", EnumSet.allOf(DispatcherType.class));
                adminDataResHandler.addServlet(new ServletHolder(new DefaultServlet()), "/*");

                HandlerCollection handlers = new HandlerCollection();
                handlers.setHandlers(new Handler[]{adminTemplatesResHandler, adminDataResHandler, rootHandler});
                server.setHandler(handlers);
                server.start();

                final ServerConnector connector = (ServerConnector) server.getConnectors()[0];
                serverPort = connector.getLocalPort();
            }
        } catch (Exception e) {
            logger.error("Exception in building AdminResourcesContainer ", e);
        }
    }

    private void initAdminContainerConfigIfNeeded() {
        if (adminContainerConfig == null) {
            adminContainerConfig = new AdminConfigImpl();
        }
    }

    private void initAdminRegistryIfNeeded() {
        if (adminPageRegistry == null) {
            adminPageRegistry = new AdminPageRegistry();
        }
    }

    public int getServerPort() {
        return serverPort;
    }

    public AdminPageRegistry getAdminPageRegistry() {
        return adminPageRegistry;
    }

    private Module getAdditionalBindings() {
        return new AbstractModule() {
            @Override
            protected void configure() {
                bind(AdminResourcesFilter.class);
            }
        };
    }

    private String appendCoreJerseyPackages(String jerseyResourcePkgListForAdminPages) {
        String pkgPath = adminContainerConfig.jerseyResourcePkgList();
        if (jerseyResourcePkgListForAdminPages != null && !jerseyResourcePkgListForAdminPages.isEmpty()) {
            pkgPath += ";" + jerseyResourcePkgListForAdminPages;
        }

        return pkgPath;
    }

    private List<Module> buildAdminPluginsGuiceModules() {
        List<Module> guiceModules = new ArrayList<>();
        if (adminPageRegistry != null) {
            final Collection<AdminPageInfo> allPages = adminPageRegistry.getAllPages();
            for (AdminPageInfo adminPlugin : allPages) {
                final List<Module> guiceModuleList = adminPlugin.getGuiceModules();
                if (guiceModuleList != null && !guiceModuleList.isEmpty()) {
                    guiceModules.addAll(adminPlugin.getGuiceModules());
                }
            }
        }
        guiceModules.add(getAdditionalBindings());
        return guiceModules;
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

