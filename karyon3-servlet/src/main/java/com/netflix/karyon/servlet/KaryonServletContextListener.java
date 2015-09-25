package com.netflix.karyon.servlet;

import javax.servlet.ServletContextEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;
import com.google.inject.ProvisionException;
import com.google.inject.servlet.GuiceServletContextListener;
import com.netflix.governator.LifecycleInjector;

/**
 * An extension of {@link GuiceServletContextListener} which integrates with Karyon/Governator's
 * Injector.  This implementation drives shutdown of LifecycleManager through the 
 * ServletContextListener's contextDestroyed event.  
 * 
 * To use, subclass your main server class from GovernatorServletContextListener
 * <code>
package com.cloudservice.StartServer;
public class StartServer extends KaryonServletContextListener
{
    {@literal @}Override
    protected LifecycleInjector createInjector() {
        return Governator.createInjector(
            new JerseyServletModule() {
                {@literal @}Override
                protected void configureServlets() {
                    serve("/REST/*").with(GuiceContainer.class);
                    binder().bind(GuiceContainer.class).asEagerSingleton();
                    
                    bind(MyResource.class).asEagerSingleton();
                }
            }
        );
    }
}
 * </code>
 * 
 * Then reference this class from web.xml.
 *
 * <code>
     &lt;filter&gt;
         &lt;filter-name&gt;guiceFilter&lt;/filter-name&gt;
         &lt;filter-class&gt;com.google.inject.servlet.GuiceFilter&lt;/filter-class&gt;
     &lt;/filter&gt;

     &lt;filter-mapping&gt;
         &lt;filter-name&gt;guiceFilter&lt;/filter-name&gt;
         &lt;url-pattern&gt;/*&lt;/url-pattern&gt;
     &lt;/filter-mapping&gt;

     &lt;listener&gt;
         &lt;listener-class&gt;com.cloudservice.StartServer&lt;/listener-class&gt;
     &lt;/listener&gt;

 * </code>
 *
 * @author Eran Landau
 */
public abstract class KaryonServletContextListener extends GuiceServletContextListener {
    protected static final Logger LOG = LoggerFactory.getLogger(KaryonServletContextListener.class);

    static final String INJECTOR_NAME = Injector.class.getName();

    private LifecycleInjector injector;
    
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        super.contextInitialized(servletContextEvent);
    }

    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        super.contextDestroyed(servletContextEvent);
        if (injector != null) {
            injector.shutdown();
        }
    }

    /**
     * Override this method to create (or otherwise obtain a reference to) your
     * injector.
     */
    protected final Injector getInjector() {
        try {
            return injector = createInjector();
        }
        catch (ProvisionException e) {
            LOG.error("Failed to created injector", e);
            throw e;
        }
        catch (Exception e) {
            LOG.error("Failed to created injector", e);
            throw new ProvisionException("Failed to create injector", e);
        }
    }
    
    protected abstract LifecycleInjector createInjector() throws Exception;

}