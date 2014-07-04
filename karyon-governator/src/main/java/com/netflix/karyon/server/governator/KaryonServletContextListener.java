package com.netflix.karyon.server.governator;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.servlet.GuiceServletContextListener;
import com.netflix.governator.guice.LifecycleInjector;
import com.netflix.governator.lifecycle.LifecycleManager;

/**
 * An extension of {@link GuiceServletContextListener} to hook on to the guice servlet module. In order for this to work you must have
 * the following entries in your web.xml for the webapp.
 *
 <PRE>
     &lt;context-param&gt;
       &lt;param-name&gt;bootstrapClass&lt;/param-name&gt;
       &lt;param-value&gt;com.myorg.MyBootstrap&lt;/param-value&gt;
     &lt;/context-param&gt;
  
     &lt;filter&gt;
         &lt;filter-name&gt;guiceFilter&lt;/filter-name&gt;
         &lt;filter-class&gt;com.google.inject.servlet.GuiceFilter&lt;/filter-class&gt;
     &lt;/filter&gt;

     &lt;filter-mapping&gt;
         &lt;filter-name&gt;guiceFilter&lt;/filter-name&gt;
         &lt;url-pattern&gt;/*&lt;/url-pattern&gt;
     &lt;/filter-mapping&gt;

     &lt;listener&gt;
         &lt;listener-class&gt;com.netflix.karyon.server.governator.KaryonServletContextListener&lt;/listener-class&gt;
     &lt;/listener&gt;

 </PRE>
 *
 * This implementation uses Governator's annotation based bootstrap model to 
 * pull in all the bindings for the server.  A bootstrap class must therefore be
 * specified in a configuration.  The default implementation expects a servlet
 * context attribute called 'bootstrapClass' in web.xml. 
 *
 * @author Eran Landau
 */
public class KaryonServletContextListener extends GuiceServletContextListener {
    protected static final Logger LOG = LoggerFactory.getLogger(KaryonServletContextListener.class);

    private static final String BOOTSTRAP_CLASS = "bootstrapClass";
    private static final String SYS_ENV_BOOTSTRAP_CLASS = "karyon.bootstrapClass";

    private ServletContextListener delegate;
    private LifecycleManager lifecycleManager;
    private Class<?> bootstrapClass;
    
    @Override
    protected Injector getInjector() {
        Injector injector = LifecycleInjector.bootstrap(bootstrapClass);
        this.lifecycleManager = injector.getInstance(LifecycleManager.class);
        if (injector.getExistingBinding(Key.get(ServletContextListener.class)) != null) {
            this.delegate = injector.getInstance(ServletContextListener.class);
        }
        return injector;
    }
    
    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        try {
            bootstrapClass = getBootstrapClass(servletContextEvent.getServletContext());
            
            super.contextInitialized(servletContextEvent);
            try {
                lifecycleManager.start();
            } catch (Exception e) {
                throw new RuntimeException("Failed to start LifecycleManager", e);
            }
            if (delegate != null) {
                delegate.contextInitialized(servletContextEvent);
            }
        }
        catch (Throwable t) {
            LOG.error("Failed to start server", t);
            System.exit(1);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        if (delegate != null) {
            delegate.contextDestroyed(servletContextEvent);
        }
        if (lifecycleManager != null) {
            lifecycleManager.close();
        }
        super.contextDestroyed(servletContextEvent);
    }
    
    /**
     * Default implementation for getting the bootstrap class from the ServletContext in the web.xml
     * or from system properties
     * 
     * @param servletContext
     * @return
     * @throws ClassNotFoundException
     */
    protected Class<?> getBootstrapClass(ServletContext servletContext) throws ClassNotFoundException {
        String bootstrapClassName = servletContext.getInitParameter(BOOTSTRAP_CLASS).toString();
        if (bootstrapClassName == null) {
            bootstrapClassName = System.getProperty(SYS_ENV_BOOTSTRAP_CLASS);
        }
        
        if (bootstrapClassName == null) {
            throw new RuntimeException("Unable to determine the bootstrap class name");
        }
                
        return Class.forName(bootstrapClassName);
    }
}
