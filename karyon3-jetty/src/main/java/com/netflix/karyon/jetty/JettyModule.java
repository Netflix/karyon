package com.netflix.karyon.jetty;

import java.util.EnumSet;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.DispatcherType;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.ProvisionException;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.servlet.GuiceFilter;
import com.netflix.karyon.LifecycleManager;
import com.netflix.karyon.api.LifecycleShutdownSignal;
import com.netflix.karyon.spi.AbstractLifecycleListener;
import com.netflix.karyon.spi.LifecycleListener;

/**
 * Installing JettyModule will create a Jetty web server within the context
 * of the Injector and will use servlet and filter bindings from any additionally
 * installed ServletModule.
 * 
 * Note that only one Jetty server may be created in an Injector 
 */
public final class JettyModule extends AbstractModule {
    private final static Logger LOG = LoggerFactory.getLogger(JettyModule.class);
    
    /**
     * Eager singleton to start the Jetty Server
     * 
     * @author elandau
     */
    @Singleton
    public static class JettyRunner {
        @Inject
        public JettyRunner(Server server, final LifecycleManager manager) {
            LOG.info("Jetty server starting");
            try {
                server.start();
                int port = ((ServerConnector)server.getConnectors()[0]).getLocalPort();
                LOG.info("Jetty server on port {} started", port);
            } catch (Exception e) {
                try {
                    server.stop();
                }
                catch (Exception e2) {
                }
                throw new ProvisionException("Jetty server failed to start", e);
            }
        }
    }
    
    /**
     * LifecycleListener to stop Jetty Server.  This will catch shutting down 
     * Jetty when notified only through LifecycleManager#shutdown() and not via the 
     * LifecycleEvent#shutdown().
     * 
     * @author elandau
     *
     */
    @Singleton
    public static class JettyShutdown extends AbstractLifecycleListener {
        private Server server;
        
        @Inject
        public JettyShutdown(Server server) {
            this.server = server;
        }
        
        @Override
        public void onStopped() {
            LOG.info("Jetty Server shutting down");
            try {
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            server.stop();
                            LOG.info("Jetty Server shut down");
                        } catch (Exception e) {
                            LOG.warn("Failed to shut down Jetty server", e);
                        }
                    }
                });
                t.start();
            } catch (Exception e) {
                LOG.warn("Error shutting down Jetty server");
            }
        }
    }
    
    @Override
    protected void configure() {
        bind(JettyRunner.class).asEagerSingleton();
        Multibinder.newSetBinder(binder(), LifecycleListener.class).addBinding().to(JettyShutdown.class);
        bind(LifecycleShutdownSignal.class).to(JettyLifecycleShutdownSignal.class);
    }
    
    @Provides
    @Singleton
    private Server getServer(JettyConfig config) {
        Server server = new Server(config.getPort());
        ServletContextHandler servletContextHandler = new ServletContextHandler(server, "/", ServletContextHandler.SESSIONS);
        servletContextHandler.addFilter(GuiceFilter.class, "/*", EnumSet.allOf(DispatcherType.class));
        servletContextHandler.addServlet(DefaultServlet.class, "/");

        return server;
    }
    
    @Override
    public boolean equals(Object obj) {
        return getClass().equals(obj.getClass());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}