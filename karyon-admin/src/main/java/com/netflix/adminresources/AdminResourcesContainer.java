package com.netflix.adminresources;

import com.netflix.governator.annotations.Configuration;
import com.netflix.karyon.spi.Component;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.thread.QueuedThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * This class starts an embedded jetty server, listening at port specified by property
 * {@link AdminResourcesContainer#ADMIN_RESOURCES_CONTAINER_LISTEN_PORT} and defaulting to
 * {@link AdminResourcesContainer#ADMIN_RESOURCES_CONTAINER_LISTEN_PORT_DEFAULT}
 *
 * @author pkamath
 * @author Nitesh Kant
 * 
 */
@Component(disableProperty = "netflix.platform.admin.resources.disable")
public class AdminResourcesContainer {

    private static final Logger logger = LoggerFactory.getLogger(AdminResourcesContainer.class);

    public static final String ADMIN_RESOURCES_CONTAINER_LISTEN_PORT = "netflix.platform.admin.resources.port";
    public static final int ADMIN_RESOURCES_CONTAINER_LISTEN_PORT_DEFAULT = 8077;

    @Configuration(
            value = ADMIN_RESOURCES_CONTAINER_LISTEN_PORT,
            documentation = "Property defining the listen port for admin resources.",
            ignoreTypeMismatch = true
    )
    private int listenPort = ADMIN_RESOURCES_CONTAINER_LISTEN_PORT_DEFAULT;

    // packages we absolutely need
    public static final String JERSEY_RESOURCE_CORE_PACKAGES_PROP_NAME = "netflix.platform.admin.resources.core.packages";
    // packages which app owners can add
    public static final String JERSEY_RESOURCE_PACKAGES_PROP_NAME = "netflix.platform.admin.resources.packages";
    private static Server server;

    /**
     * Initializes the container and hence the embedded jetty server.
     *
     * @throws Exception if there is an issue while starting the server
     */
    @PostConstruct
    public void init() throws Exception {
        server = new Server(listenPort);
        Context root = new Context(server, "/", Context.SESSIONS);
        root.addFilter(LoggingFilter.class, "/*", Handler.DEFAULT);
/*

        ServletHolder sh = new ServletHolder(RedirectServlet.class);
        root.addServlet(sh, "/AdminNetflixConfiguration*/
/*");
        sh = new ServletHolder(ServletContainer.class);

        sh.setInitParameter("javax.ws.rs.Application", "com.netflix.adminresources.AdminResourcesApplication");

        root.addServlet(sh, "*/
/*");
*/

        // use daemon threads (QueuedThreadPool is what Server uses
        // internally as of now)
        QueuedThreadPool threadPool = new QueuedThreadPool();
        threadPool.setDaemon(true);
        server.setThreadPool(threadPool);
        server.start();
    }

    @PreDestroy
    public static void shutdown() {
        try {
            server.stop();
        } catch (Throwable t) {
            logger.warn("Error while shutting down Admin resources server", t);
        }
    }

}
