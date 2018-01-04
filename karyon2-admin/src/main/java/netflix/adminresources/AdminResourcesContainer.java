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

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.Filter;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.HandlerCollection;
import org.mortbay.jetty.handler.ResourceHandler;
import org.mortbay.jetty.security.SslSocketConnector;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.DefaultServlet;
import org.mortbay.jetty.servlet.FilterHolder;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.jetty.servlet.SessionHandler;
import org.mortbay.resource.Resource;
import org.mortbay.thread.QueuedThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Singleton;
import com.google.inject.Stage;
import com.netflix.governator.guice.LifecycleInjector;
import com.netflix.governator.lifecycle.LifecycleManager;
import com.sun.jersey.api.core.PackagesResourceConfig;

import netflix.admin.AdminConfigImpl;
import netflix.admin.AdminContainerConfig;

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

    @Inject
    public AdminResourcesContainer() {}
    
    public AdminResourcesContainer(Injector appInjector, AdminContainerConfig adminContainerConfig,
			AdminPageRegistry adminPageRegistry) {
		this.appInjector = appInjector;
		this.adminContainerConfig = adminContainerConfig;
		this.adminPageRegistry = adminPageRegistry;
	}
    
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

                if (! adminContainerConfig.shouldEnable()) {
                    return;
                }

                if (adminContainerConfig.shouldScanClassPathForPluginDiscovery()) {
                    adminPageRegistry.registerAdminPagesWithClasspathScan();
                }

                Injector adminResourceInjector;
                if (shouldShareResourcesWithParentInjector()) {
                    adminResourceInjector = appInjector.createChildInjector(buildAdminPluginsGuiceModules());
                } else {
                    adminResourceInjector = LifecycleInjector
                            .builder()
                            .inStage(Stage.DEVELOPMENT)
                            .usingBasePackages("com.netflix.explorers")
                            .withModules(buildAdminPluginsGuiceModules())
                            .build()
                            .createInjector();
                    adminResourceInjector.getInstance(LifecycleManager.class).start();
                }

                server = new Server(adminContainerConfig.listenPort());
                final List<Filter> additionaFilters = adminContainerConfig.additionalFilters();

                // redirect filter based on configurable RedirectRules
                final Context rootHandler = new Context();
                rootHandler.setContextPath("/");
                rootHandler.addFilter(new FilterHolder(adminResourceInjector.getInstance(RedirectFilter.class)), "/*", Handler.DEFAULT);
                rootHandler.addServlet(new ServletHolder(new DefaultServlet()), "/*");

                // admin page template resources
                AdminResourcesFilter arfTemplatesResources = adminResourceInjector.getInstance(AdminResourcesFilter.class);
                Map<String, Object> props = new HashMap<>(adminContainerConfig.getJerseyConfigProperties());
                props.put(PackagesResourceConfig.PROPERTY_PACKAGES, 
                        adminContainerConfig.jerseyViewableResourcePkgList() + ";" + 
                        Objects.toString(props.get(PackagesResourceConfig.PROPERTY_PACKAGES)));
                arfTemplatesResources.setProperties(props);

                logger.info("Admin templates context : {}", adminContainerConfig.templateResourceContext());
                final Context adminTemplatesResHandler = new Context();
                adminTemplatesResHandler.setContextPath(adminContainerConfig.templateResourceContext());
                adminTemplatesResHandler.setSessionHandler(new SessionHandler());
                adminTemplatesResHandler.addFilter(LoggingFilter.class, "/*", Handler.DEFAULT);
                adminTemplatesResHandler.addFilter(new FilterHolder(adminResourceInjector.getInstance(RedirectFilter.class)), "/*", Handler.DEFAULT);
                applyAdditionalFilters(adminTemplatesResHandler, additionaFilters);
                adminTemplatesResHandler.addFilter(new FilterHolder(arfTemplatesResources), "/*", Handler.DEFAULT);
                adminTemplatesResHandler.addServlet(new ServletHolder(new DefaultServlet()), "/*");
                
                // admin page data resources
                AdminResourcesFilter arfDataResources = adminResourceInjector.getInstance(AdminResourcesFilter.class);
                props = new HashMap<>(adminContainerConfig.getJerseyConfigProperties());
                props.put(PackagesResourceConfig.PROPERTY_PACKAGES, 
                        appendCoreJerseyPackages(adminPageRegistry.buildJerseyResourcePkgListForAdminPages()) + ";" + 
                        Objects.toString(props.get(PackagesResourceConfig.PROPERTY_PACKAGES)));
                arfDataResources.setProperties(props);

                logger.info("Admin resources context : {}", adminContainerConfig.ajaxDataResourceContext());
                final Context adminDataResHandler = new Context();
                adminDataResHandler.setContextPath(adminContainerConfig.ajaxDataResourceContext());
                adminDataResHandler.addFilter(LoggingFilter.class, "/*", Handler.DEFAULT);
                adminDataResHandler.addFilter(new FilterHolder(adminResourceInjector.getInstance(RedirectFilter.class)), "/*", Handler.DEFAULT);
                applyAdditionalFilters(adminDataResHandler, additionaFilters);
                adminDataResHandler.addFilter(new FilterHolder(arfDataResources), "/*", Handler.DEFAULT);
                adminDataResHandler.addServlet(new ServletHolder(new DefaultServlet()), "/*");

                QueuedThreadPool threadPool = new QueuedThreadPool();
                threadPool.setDaemon(true);
                server.setThreadPool(threadPool);

                ResourceHandler resource_handler = new ResourceHandler() {
                    @Override
                    public Resource getResource(String path) throws MalformedURLException {
                        Resource resource = Resource.newClassPathResource(path);
                        if (resource == null || !resource.exists()) {
                            resource = Resource.newClassPathResource("META-INF/resources" + path);
                        }
                        if (resource != null && resource.isDirectory()) {
                            return null;
                        }
                        return resource;
                    }                    
                };
                resource_handler.setResourceBase("/");

                HandlerCollection handlers = new HandlerCollection();
                handlers.setHandlers(new Handler[]{resource_handler, adminTemplatesResHandler, adminDataResHandler, rootHandler});
                server.setHandler(handlers);
                for (Connector connector : adminContainerConfig.additionalConnectors()) {
                    server.addConnector(connector);
                }

                server.start();

                final Connector connector = server.getConnectors()[0];
                serverPort = connector.getLocalPort();
                
                logger.info("jetty started on port {}", serverPort);
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

                if (! shouldShareResourcesWithParentInjector()) {
                    bind(AdminPageRegistry.class).toInstance(adminPageRegistry);
                    bind(AdminContainerConfig.class).toInstance(adminContainerConfig);
                }
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
                logger.info("Adding admin page {}: jersey={} modules{}", 
                        adminPlugin.getName(),
                        adminPlugin.getJerseyResourcePackageList(),
                        adminPlugin.getGuiceModules());
                final List<Module> guiceModuleList = adminPlugin.getGuiceModules();
                if (guiceModuleList != null && !guiceModuleList.isEmpty()) {
                    guiceModules.addAll(adminPlugin.getGuiceModules());
                }
            }
        }
        guiceModules.add(getAdditionalBindings());
        return guiceModules;
    }

    private boolean shouldShareResourcesWithParentInjector() {
        return appInjector != null && ! adminContainerConfig.shouldIsolateResources();
    }

    private void applyAdditionalFilters(final Context contextHandler, List<Filter> additionalFilters) {
        if (additionalFilters != null && !additionalFilters.isEmpty()) {
            for(Filter f : additionalFilters) {
                contextHandler.addFilter(new FilterHolder(f), "/*", Handler.DEFAULT);
            }
        }
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

